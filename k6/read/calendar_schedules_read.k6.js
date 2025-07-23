import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import { BASE_URLS, DEFAULT_TIME_ZONE, COMMON_PARAMS, getAuthHeaders } from '../utils/common.js';

const STAGES = [
    { duration: '2m', target: 500 },
    { duration: '1m', target: 500 },
    { duration: '2m', target:   0 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<500'],
        'group_duration{group:::Fetch Schedules by Date Range}': ['p(95)<500'],
    },
};

const TODAY = new Date();
const START_DATE = `${TODAY.getFullYear()}-${String(TODAY.getMonth() + 1).padStart(2, '0')}-01`;
const NEXT_MONTH = new Date(TODAY.getFullYear(), TODAY.getMonth() + 1, 1);
const END_DATE = `${NEXT_MONTH.getFullYear()}-${String(NEXT_MONTH.getMonth() + 1).padStart(2, '0')}-01`;

export function setup() {
    console.log('== Setup phase: Pre-authenticating users for Calendar Schedules test... ==');
    const maxVUs = STAGES
        .map((s) => Number(s.target))
        .reduce((max, v) => Math.max(max, v), 0);
    const users = [];
    const tokenExpSeconds = 3600;

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-sched-${i}`);
        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            throw new Error(`[FATAL] Failed to sign in user: ${email} during setup.`);
        }
    }
    console.log(`== Setup complete: ${users.length} users authenticated. ==`);
    return users;
}

/**
 * 지정된 기간 내의 일정 목록을 조회하고 응답을 검증하는 함수.
 * @param {string} authToken - 인증 토큰
 */
function fetchAndCheckSchedules(authToken) {
    const url = `${BASE_URLS.CALENDAR_SCHEDULES}?startDate=${START_DATE}&endDate=${END_DATE}&userTimeZone=${DEFAULT_TIME_ZONE}`;

    const res = http.get(url, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'GetSchedulesByDateRange' },
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response data is an array': (r) => Array.isArray(r.json('data')),
        'first schedule has scheduleId (if exists)': (r) => {
            const data = r.json('data');
            return Array.isArray(data) ? (data.length > 0 ? data[0].scheduleId != null : true) : false;
        },
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetSchedules. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    }
}

export default function (users) {
    const currentUser = users[exec.vu.idInTest - 1];
    if (!currentUser) { return; }

    group('Fetch Schedules by Date Range', function () {
        fetchAndCheckSchedules(currentUser.accessToken);
    });

    sleep(1);
}