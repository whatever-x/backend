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
        'http_req_duration': ['p(95)<600'],
        'group_duration{group:::Fetch Memo List}': ['p(95)<700'],
        'group_duration{group:::Fetch Single Memo}': ['p(95)<400'],
    },
};

export function setup() {
    console.log('== Setup phase: Pre-authenticating users for Memo test... ==');
    const maxVUs = STAGES
        .map((s) => Number(s.target))
        .reduce((max, v) => Math.max(max, v), 0);
    const users = [];
    const tokenExpSeconds = 3600;

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-memo-${i}`);

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
 * 메모 목록을 조회하고, 결과를 반환합니다.
 * @returns {Array} 메모 목록 배열 또는 빈 배열
 */
function getMemoList(authToken) {
    let url = `${BASE_URLS.CONTENT}/memo`;

    const res = http.get(url, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'GetMemoList' }
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has list array': (r) => Array.isArray(r.json('data.list')),
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetMemoList. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
        return []; // 실패 시 빈 배열 반환
    }

    return res.json('data.list') || [];
}

/**
 * 특정 ID의 메모를 조회하고 검증합니다.
 */
function fetchSingleMemo(authToken, memoId) {
    const url = `${BASE_URLS.CONTENT}/memo/${memoId}`;
    const res = http.get(url, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'GetSingleMemo' }
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has correct id': (r) => r.json('data.id') == memoId,
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetSingleMemo. VU: ${__VU}, ID: ${memoId}, Status: ${res.status}, Body: ${res.body}`);
    }
}

export default function (users) {
    const currentUser = users[exec.vu.idInTest - 1];
    if (!currentUser) { return; }

    const authToken = currentUser.accessToken;
    let memoToRead = null;

    // 1. 메모 목록 조회 시나리오
    group('Fetch Memo List', function () {
        const memos = getMemoList(authToken);

        if (memos.length > 0) {
            memoToRead = memos[0]; // 첫 번째 메모를 선택
        }
    });

    sleep(1);

    if (memoToRead) {
        // 2. 단건 메모 조회 시나리오
        group('Fetch Single Memo', function () {
            fetchSingleMemo(authToken, memoToRead.id);
        });
    }

    sleep(1);
}