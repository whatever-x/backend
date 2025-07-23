import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import {
    BASE_URLS,
    COMMON_PARAMS,
    getAuthHeaders,
    JSON_CONTENT_TYPE_HEADER
} from '../utils/common.js';

const STAGES = [
    { duration: '2m', target: 200 },
    { duration: '1m', target: 200 },
    { duration: '2m', target: 0 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<600'],
        'group_duration{group:::1. Create Memo}': ['p(95)<500'],
        'group_duration{group:::2. Read Single Memo}': ['p(95)<500'],
        'group_duration{group:::3. Read Memo List}': ['p(95)<700'],
        'group_duration{group:::4. Update Memo}': ['p(95)<500'],
        'group_duration{group:::5. Delete Memo}': ['p(95)<500'],
    },
};

export function setup() {
    console.log('== Setup phase: Pre-authenticating users for Memo CRUD test... ==');
    const maxVUs = STAGES.reduce((max, s) => Math.max(max, Number(s.target)), 0);
    const users = [];
    const tokenExpSeconds = 3600;

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-crud-${i}`);
        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            throw new Error(`[FATAL] Failed to sign in user: ${email} during setup.`);
        }
    }
    console.log(`== Setup complete: ${users.length} users authenticated. ==`);
    return users;
}

function createMemo(authToken, memoData) {
    const res = http.post(`${BASE_URLS.CONTENT}/memo`, JSON.stringify(memoData), {
        headers: {
            ...JSON_CONTENT_TYPE_HEADER,
            ...getAuthHeaders(authToken),
        },
        tags: { name: 'CreateMemo' }
    });
    let contentId = null;
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has contentId': (r) => {
            const id = r.json('data.id');
            if (id != null) { contentId = id; return true; }
            return false;
        }
    });
    if (!isSuccess) console.error(`[FAIL] CreateMemo. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return contentId;
}

function getMemo(authToken, memoId) {
    const res = http.get(`${BASE_URLS.CONTENT}/memo/${memoId}`, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetSingleMemo' } });
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has correct id': (r) => r.json('data.id') === memoId,
    });
    if (!isSuccess) console.error(`[FAIL] GetSingleMemo. VU: ${__VU}, ID: ${memoId}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

function getMemoList(authToken, queryParams) {
    let url = `${BASE_URLS.CONTENT}/memo`;

    const res = http.get(url, {
        headers: getAuthHeaders(authToken),
        tags: { name: 'GetMemoList' }
    });
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has list array': (r) => Array.isArray(r.json('data.list')),
    });
    if (!isSuccess) console.error(`[FAIL] GetMemoList. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

function updateMemo(authToken, memoId, updateData) {
    const res = http.put(`${BASE_URLS.CONTENT}/memo/${memoId}`, JSON.stringify(updateData), {
        headers: {
            ...JSON_CONTENT_TYPE_HEADER,
            ...getAuthHeaders(authToken),
        },
        tags: { name: 'UpdateMemo' }
    });
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has correct id': (r) => r.json('data.id') == memoId,
    });
    if (!isSuccess) console.error(`[FAIL] UpdateMemo. VU: ${__VU}, ID: ${memoId}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

function deleteMemo(authToken, memoId) {
    const res = http.del(`${BASE_URLS.CONTENT}/memo/${memoId}`, null, {
        headers: {
            ...JSON_CONTENT_TYPE_HEADER,
            ...getAuthHeaders(authToken),
        },
        tags: { name: 'DeleteMemo' }
    });
    const isSuccess = check(res, {
        'status is 200 or 204': (r) => r.status === 200 || r.status === 204,
    });
    if (!isSuccess) console.error(`[FAIL] DeleteMemo. VU: ${__VU}, ID: ${memoId}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}


export default function (users) {
    const iteration = exec.scenario.iterationInTest;
    const currentUser = users[iteration % users.length];
    if (!currentUser) { return; }

    const authToken = currentUser.accessToken;
    let createdMemoId = null;

    // 1. 메모 생성
    group('1. Create Memo', function () {
        const createMemoData = {
            title: `VU ${__VU} Iter ${iteration}`,
            description: 'This memo is for CRUD performance testing.',
            tags: [1, 2],
        };
        createdMemoId = createMemo(authToken, createMemoData);
    });

    sleep(1);

    if (createdMemoId) {
        // 2. 생성된 메모 단건 조회
        group('2. Read Single Memo', function () {
            getMemo(authToken, createdMemoId);
        });
        sleep(1);

        // 3. 메모 목록 조회
        group('3. Read Memo List', function () {
            getMemoList(authToken, { size: 5 });
        });
        sleep(1);

        // 4. 메모 수정
        group('4. Update Memo', function () {
            const updateMemoData = {
                title: `UPDATED k6 - VU ${__VU} Iter ${iteration}`,
                description: '(Updated) This memo is for CRUD performance testing.',
            };
            updateMemo(authToken, createdMemoId, updateMemoData);
        });
        sleep(1);

        // 5. 메모 삭제
        group('5. Delete Memo', function () {
            deleteMemo(authToken, createdMemoId);
        });
        sleep(1);
    }
}