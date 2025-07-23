import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { signInWithTestEmail } from '../utils/auth.js';
import {
    BASE_URLS,
    COMMON_PARAMS,
    getAuthHeaders,
    JSON_CONTENT_TYPE_HEADER
} from '../utils/common.js';

const STAGES = [
    { duration: '2m', target: 20 },
    { duration: '1m', target: 20 },
    { duration: '2m', target: 0 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<1000'],
        'group_duration{group:::1. Sign-Up and Login}': ['p(95)<700'],
        'group_duration{group:::2. Create Invitation Code}': ['p(95)<500'],
        'group_duration{group:::3. Connect Couple}': ['p(95)<800'],
        'group_duration{group:::4. Leave Couple}': ['p(95)<700'],
    },
};

const SIGNUP_URL = `${BASE_URLS.SAMPLE}/test/sign-up/single`;
const API_BASE = BASE_URLS.COUPLE;

function signUpNewUser() {
    const res = http.get(SIGNUP_URL, { headers: { 'accept': 'application/json' }, ...COMMON_PARAMS, tags: { name: 'SignUpNewUser' } });
    const isSuccess = check(res, { 'SignUp: status is 200': (r) => r.status === 200 });
    if (!isSuccess) console.error(`[FAIL] SignUpNewUser. Status: ${res.status}, Body: ${res.body}`);
    return isSuccess ? res.json('data') : null;
}
function createInvitationCode(authToken) {
    const res = http.post(`${API_BASE}/invitation-code`, null, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'CreateInvitationCode' } });
    const isSuccess = check(res, { 'CreateCode: status is 200': (r) => r.status === 200 });
    if (!isSuccess) console.error(`[FAIL] CreateCode. Status: ${res.status}, Body: ${res.body}`);
    return isSuccess ? res.json('data.invitationCode') : null;
}
function connectCouple(authToken, invitationCode) {
    const payload = JSON.stringify({ invitationCode });
    const params = { headers: { ...JSON_CONTENT_TYPE_HEADER, ...getAuthHeaders(authToken) }, ...COMMON_PARAMS, tags: { name: 'ConnectCouple' } };
    const res = http.post(`${API_BASE}/connect`, payload, params);
    const isSuccess = check(res, { 'Connect: status is 200': (r) => r.status === 200 });
    if (!isSuccess) console.error(`[FAIL] ConnectCouple. Status: ${res.status}, Body: ${res.body}`);
    return isSuccess ? res.json('data.coupleId') : null;
}
function leaveCouple(authToken, coupleId) {
    const res = http.del(`${API_BASE}/${coupleId}/members/me`, null, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'LeaveCouple' } });
    const isSuccess = check(res, { 'Leave: status is 200 or 204': (r) => r.status === 200 || r.status === 204 });
    if (!isSuccess) console.error(`[FAIL] LeaveCouple. Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}

export default function () {
    let emailA = null, emailB = null;

// === 1. 신규 사용자 2명 생성 및 로그인 ===
    let userAToken, userBToken;
    group('1. Sign-Up and Login', function () {
        emailA = signUpNewUser();
        if (emailA) userAToken = signInWithTestEmail(emailA, 3600)?.accessToken;
        emailB = signUpNewUser();
        if (emailB) userBToken = signInWithTestEmail(emailB, 3600)?.accessToken;
    });
    if (!userAToken || !userBToken) {
        console.error(`[ABORT] Login failed. A: ${emailA}, B: ${emailB}`);
        return; // finally 블록으로 이동하여 생성된 사용자 삭제
    }
    sleep(1);

    // === 2. 사용자 A가 초대 코드 생성 ===
    let invitationCode = null;
    group('2. Create Invitation Code', function () {
        invitationCode = createInvitationCode(userAToken);
    });
    if (!invitationCode) {
        console.error(`[ABORT] Invitation code creation failed for user A: ${emailA}`);
        return; // finally 블록으로 이동
    }
    sleep(1);

    // === 3. 사용자 B가 코드로 커플 연결 ===
    let coupleId = null;
    group('3. Connect Couple', function () {
        coupleId = connectCouple(userBToken, invitationCode);
    });
    if (!coupleId) {
        console.error(`[ABORT] Couple connection failed for user B: ${emailB}`);
        return; // finally 블록으로 이동
    }
    sleep(1);

    // === 4. 사용자 B가 커플 나가기 ===
    group('4. Leave Couple', function () {
        leaveCouple(userBToken, coupleId);
    });
    sleep(1);
}