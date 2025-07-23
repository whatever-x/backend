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
    { duration: '2m', target: 100 },
    { duration: '1m', target: 100 },
    { duration: '2m', target: 0 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<1000'],
        'group_duration{group:::Update Couple Info}': ['p(95)<1000'],
    },
};

const API_BASE = BASE_URLS.COUPLE;

function getMyCoupleInfo(authToken) {
    const res = http.get(`${API_BASE}/me`, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetMyCoupleInfo' } });
    const isSuccess = check(res, { 'GetInfo: status is 200': (r) => r.status === 200 });
    if (!isSuccess) console.error(`[FAIL] GetMyInfo. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess ? res.json('data.coupleId') : null;
}
function updateStartDate(authToken, coupleId, newDate) {
    const payload = JSON.stringify({ startDate: newDate });
    const params = { headers: { ...JSON_CONTENT_TYPE_HEADER, ...getAuthHeaders(authToken), 'Time-Zone': 'Asia/Seoul' }, ...COMMON_PARAMS, tags: { name: 'UpdateStartDate' } };
    const res = http.patch(`${API_BASE}/${coupleId}/start-date`, payload, params);
    const isSuccess = check(res, { 'UpdateDate: status is 200': (r) => r.status === 200, 'UpdateDate: matches': (r) => r.json('data.startDate') === newDate });
    if (!isSuccess) console.error(`[FAIL] UpdateStartDate. Status: ${res.status}, Body: ${res.body}`);
}
function updateSharedMessage(authToken, coupleId, newMsg) {
    const payload = JSON.stringify({ sharedMessage: newMsg });
    const params = { headers: { ...JSON_CONTENT_TYPE_HEADER, ...getAuthHeaders(authToken) }, ...COMMON_PARAMS, tags: { name: 'UpdateSharedMessage' } };
    const res = http.patch(`${API_BASE}/${coupleId}/shared-message`, payload, params);
    const isSuccess = check(res, { 'UpdateMsg: status is 200': (r) => r.status === 200, 'UpdateMsg: matches': (r) => r.json('data.sharedMessage') === newMsg });
    if (!isSuccess) console.error(`[FAIL] UpdateSharedMessage. Status: ${res.status}, Body: ${res.body}`);
}


export function setup() {
    console.log('== Setup phase: Pre-authenticating existing user pairs... ==');
    const maxVUs = STAGES.reduce((max, s) => Math.max(max, Number(s.target)), 0);
    const userPairs = [];

    // 더미 데이터 생성 시 user (i*2+1)과 (i*2+2)는 커플임 (예: 1-2, 3-4, ...)
    for (let i = 0; i < maxVUs; i++) {
        const userNumberA = i * 2 + 1;
        const userNumberB = i * 2 + 2;
        const emailA = `user${userNumberA}@ex.com`;
        const emailB = `user${userNumberB}@ex.com`;

        // 1. 사용자 A, B 로그인
        const tokensA = signInWithTestEmail(emailA, 3600);
        const tokensB = signInWithTestEmail(emailB, 3600);
        if (!tokensA || !tokensB) throw new Error(`[FATAL] Failed to sign in user pair: ${emailA}, ${emailB}`);

        // 2. 사용자 A의 토큰으로 coupleId 조회
        const coupleId = getMyCoupleInfo(tokensA.accessToken);
        if (!coupleId) throw new Error(`[FATAL] User ${emailA} is not in a couple or failed to fetch info.`);

        userPairs.push({
            userA: { email: emailA, accessToken: tokensA.accessToken },
            userB: { email: emailB, accessToken: tokensB.accessToken },
            coupleId: coupleId,
        });
    }
    console.log(`== Setup complete: ${userPairs.length} user pairs prepared. ==`);
    return userPairs;
}

export default function (userPairs) {
    const currentPair = userPairs[exec.vu.idInTest - 1];
    if (!currentPair) { return; }

    const userAToken = currentPair.userA.accessToken;
    const coupleId = currentPair.coupleId;
    const iteration = exec.scenario.iterationInTest;

    // === 사용자 A가 커플 정보 수정 ===
    group('Update Couple Info', function () {
        const dayOffset = (iteration % 28) + 1;
        const newDate = `2024-01-${String(dayOffset).padStart(2, '0')}`;
        updateStartDate(userAToken, coupleId, newDate);

        sleep(0.5);

        const newMsg = `VU:${exec.vu.idInTest} Itr:${iteration}`;
        updateSharedMessage(userAToken, coupleId, newMsg);
    });

    sleep(1);
}