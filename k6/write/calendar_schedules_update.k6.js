import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import {
    BASE_URLS,
    COMMON_PARAMS,
    getAuthHeaders, JSON_CONTENT_TYPE_HEADER,
} from '../utils/common.js';

const STAGES = [
    { duration: '2m', target: 200 },
    { duration: '1m', target: 200 },
    { duration: '2m', target: 200 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<600'],
        'group_duration{group:::1. Create Schedule}': ['p(95)<600'],
        'group_duration{group:::2. Update Schedule}': ['p(95)<600'],
        'group_duration{group:::3. Delete Schedule}': ['p(95)<500'],
    },
};

export function setup() {
    console.log('== Setup phase: Pre-authenticating users for Calendar CUD test... ==');
    const maxVUs = STAGES.reduce((max, s) => Math.max(max, Number(s.target)), 0);
    const users = [];
    const tokenExpSeconds = 3600;

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-cud-${i}`);
        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            throw new Error(`[FATAL] Failed to sign in user: ${email} during setup.`);
        }
    }
    console.log(`== Setup complete: ${users.length} users authenticated. ==`);
    return users;
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
        'response has content-id': (r) => {
            const id = r.json('data.id');
            if (id != null) { contentId = id; return true; }
            return false;
        }
    });
    if (!isSuccess) console.error(`[FAIL] CreateSchedule. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return contentId;
}

function updateSchedule(authToken, scheduleId, updateData) {
    const res = http.put(`${BASE_URLS.CALENDAR_SCHEDULES}/${scheduleId}`, JSON.stringify(updateData), {
        headers: {
            ...JSON_CONTENT_TYPE_HEADER,
            ...getAuthHeaders(authToken),
        },
        tags: { name: 'UpdateSchedule' }
    });
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
    });
    if (!isSuccess) console.error(`[FAIL] UpdateSchedule. VU: ${__VU}, ID: ${scheduleId}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

function deleteSchedule(authToken, scheduleId) {
    const res = http.del(`${BASE_URLS.CALENDAR_SCHEDULES}/${scheduleId}`, null, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'DeleteSchedule' }
    });
    const isSuccess = check(res, {
        'status is 200 or 204 (No Content)': (r) => r.status === 200 || r.status === 204,
    });
    if (!isSuccess) console.error(`[FAIL] DeleteSchedule. VU: ${__VU}, ID: ${scheduleId}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

export default function (users) {
    const iteration = exec.scenario.iterationInTest;
    const currentUser = users[iteration % users.length];
    if (!currentUser) { return; }

    const authToken = currentUser.accessToken;
    let createdScheduleId = null;
    let success = true;

    // 1. 새로운 일정 생성
    group('1. Create Schedule', function () {
        const newSchedule = {
            title: `VU ${__VU} Iter ${iteration}`, // 매번 고유한 제목 생성
            description: 'This is a schedule created for CUD performance testing.',
            startDateTime: '2025-02-16T18:26:40', startTimeZone: 'Asia/Seoul',
            endDateTime: '2025-02-16T23:59:59', endTimeZone: 'Asia/Seoul',
            tagIds: [1, 2, 3],
        };
        createdScheduleId = createSchedule(authToken, newSchedule);
    });

    sleep(1);

    // 생성이 성공했을 경우에만 수정 및 삭제 진행
    if (createdScheduleId) {
        // 2. 생성된 일정 수정
        group('2. Update Schedule', function () {
            const updateData = {
                selectedDate: '2025-02-16',
                title: `UPDATED-VU ${__VU} Iter ${iteration}`,
                description: '(Updated) This is a schedule created for CUD performance testing.',
                startDateTime: '2025-02-17T10:00:00', startTimeZone: 'Asia/Seoul',
                endDateTime: '2025-02-17T11:00:00', endTimeZone: 'Asia/Seoul',
                tagIds: [4, 5],
            };
            success = updateSchedule(authToken, createdScheduleId, updateData);
        });

        sleep(1);

        // 3. 수정된 일정 삭제 (수정이 실패했더라도 삭제는 시도)
        group('3. Delete Schedule', function () {
            deleteSchedule(authToken, createdScheduleId);
        });
    }

    sleep(1);
}