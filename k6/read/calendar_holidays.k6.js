import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import { BASE_URLS, COMMON_PARAMS, getAuthHeaders } from '../utils/common.js';

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
        'group_duration{group:::Fetch Monthly Holidays}': ['p(95)<500'],
        'group_duration{group:::Fetch Yearly Holidays}': ['p(95)<600'],
    },
};

const TODAY = new Date();
const CURRENT_YEAR = TODAY.getFullYear();
const CURRENT_MONTH_STR = String(TODAY.getMonth() + 1).padStart(2, '0');
const NEXT_MONTH_DATE = new Date(CURRENT_YEAR, TODAY.getMonth() + 1, 1);
const NEXT_YEAR_MONTH_STR = `${NEXT_MONTH_DATE.getFullYear()}-${String(NEXT_MONTH_DATE.getMonth() + 1).padStart(2, '0')}`;
const START_YEAR_MONTH = `${CURRENT_YEAR}-${CURRENT_MONTH_STR}`;

export function setup() {
    console.log('== Setup phase: Pre-authenticating users for Balance Game test... ==');
    const maxVUs = STAGES
        .map((s) => Number(s.target))
        .reduce((max, v) => Math.max(max, v), 0);

    const users = [];
    const tokenExpSeconds = 3600;

    if (maxVUs === 0) {
        return users;
    }

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-bg-${i}`);
        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            throw new Error(`Failed to sign in user: ${email} during setup.`);
        }
    }
    console.log(`== Setup complete: ${users.length} users authenticated. ==`);
    return users;
}

function fetchHolidaysByMonth(authToken) {
    const url = `${BASE_URLS.CALENDAR_HOLIDAYS}/holidays?startYearMonth=${START_YEAR_MONTH}&endYearMonth=${NEXT_YEAR_MONTH_STR}`;
    const res = http.get(url, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'GetHolidaysByMonth' }
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has holidayList array': (r) => Array.isArray(r.json('data.holidayList')),
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetHolidaysByMonth. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    }
}

function fetchHolidaysByYear(authToken) {
    const url = `${BASE_URLS.CALENDAR_HOLIDAYS}/holidays/year?year=${CURRENT_YEAR}`;
    const res = http.get(url, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'GetHolidaysByYear' }
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has holidayList array': (r) => Array.isArray(r.json('data.holidayList')),
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetHolidaysByYear. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    }
}

export default function (users) {
    const currentUser = users[exec.vu.idInTest - 1];
    if (!currentUser) { return; }

    group('Fetch Monthly Holidays', function () {
        fetchHolidaysByMonth(currentUser.accessToken);
    });

    sleep(1);

    group('Fetch Yearly Holidays', function () {
        fetchHolidaysByYear(currentUser.accessToken);
    });

    sleep(1);
}