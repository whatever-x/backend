import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import {
    BASE_URLS,
    DEFAULT_TIME_ZONE,
    COMMON_PARAMS,
    getAuthHeaders,
    JSON_CONTENT_TYPE_HEADER
} from '../utils/common.js';

const STAGES = [
    { duration: '2m', target: 200 },
    { duration: '1m', target: 200 },
    { duration: '2m', target:   0 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<600'],
        'group_duration{group:::1. Fetch Schedule List}': ['p(95)<500'],
        'group_duration{group:::2. Create New Schedule}': ['p(95)<600'],
        'group_duration{group:::3. Fetch Created Schedule}': ['p(95)<400'],
    },
};

const TODAY = new Date();
const START_DATE = `${TODAY.getFullYear()}-${String(TODAY.getMonth() + 1).padStart(2, '0')}-01`;
const END_DATE = `${new Date(TODAY.getFullYear(), TODAY.getMonth() + 1, 1).toISOString().substring(0, 8)}01`;

export function setup() {
    console.log('== Setup phase: Pre-authenticating users... ==');
    const maxVUs = STAGES
        .map((s) => Number(s.target))
        .reduce((max, v) => Math.max(max, v), 0);
    const users = [];
    const tokenExpSeconds = 3600;

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-couple-${i}`);

        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            throw new Error(`[FATAL] Failed to sign in user: ${email} during setup. This user should exist and be in a couple.`);
        }
    }
    console.log(`== Setup complete: ${users.length} coupled users authenticated. ==`);
    return users;
}

function getSchedulesList(authToken) {
    const url = `${BASE_URLS.CALENDAR_SCHEDULES}?startDate=${START_DATE}&endDate=${END_DATE}&userTimeZone=${DEFAULT_TIME_ZONE}`;
    const res = http.get(url, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetScheduleList' } });
    const isSuccess = check(res, { 'status is 200': (r) => r.status === 200, 'response data is array': (r) => Array.isArray(r.json('data')) });
    if (!isSuccess) console.error(`[FAIL] GetScheduleList. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

function createSchedule(authToken, scheduleData) {
    const res = http.post(`${BASE_URLS.CALENDAR_SCHEDULES}`, JSON.stringify(scheduleData), {
        headers: {
            ...JSON_CONTENT_TYPE_HEADER,
            ...getAuthHeaders(authToken),
        },
        tags: { name: 'CreateSchedule' }
    });
    let contentId = null;
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has id': (r) => {
            const id = r.json('data.id');
            if (id != null) { contentId = id; return true; }
            return false;
        }
    });
    if (!isSuccess) console.error(`[FAIL] CreateSchedule. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return contentId;
}

function getScheduleById(authToken, scheduleId) {
    const res = http.get(`${BASE_URLS.CALENDAR_SCHEDULES}/${scheduleId}`, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetScheduleById' } });
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has scheduleId that matches': (r) => r.json('data.scheduleDetail.scheduleId') === scheduleId,
    });
    if (!isSuccess) console.error(`[FAIL] GetScheduleById. VU: ${__VU}, ID: ${scheduleId}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

export default function (users) {
    const iteration = exec.scenario.iterationInTest;
    const currentUser = users[iteration % users.length];
    if (!currentUser) { return; }

    const authToken = currentUser.accessToken;
    let createdScheduleId = null;

    // 1. 일정 목록 조회
    group('1. Fetch Schedule List', function () {
        getSchedulesList(authToken);
    });
    sleep(1);

    // 2. 새로운 일정 생성
    group('2. Create New Schedule', function () {
        const newSchedule = {
            title: `VU ${__VU} Iter ${iteration}`, // 매번 고유한 제목 생성
            description: 'This is a schedule created for performance testing.',
            startDateTime: '2025-02-16T18:26:40', startTimeZone: 'Asia/Seoul',
            endDateTime: '2025-02-16T23:59:59', endTimeZone: 'Asia/Seoul',
            tagIds: [1, 2, 3],
        };
        createdScheduleId = createSchedule(authToken, newSchedule);
    });
    sleep(1);

    // 3. 생성이 성공했을 경우에만 단건 조회 진행
    if (createdScheduleId) {
        group('3. Fetch Created Schedule', function () {
            getScheduleById(authToken, createdScheduleId);
        });
    }
    sleep(1);
}