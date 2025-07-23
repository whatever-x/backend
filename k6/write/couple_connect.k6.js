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
        'group_duration{group:::4. Get My Info}': ['p(95)<400'],
    },
};

const SIGNUP_URL = `${BASE_URLS.SAMPLE}/test/sign-up/single`;

function signUpNewUser() {
    const res = http.get(SIGNUP_URL, { headers: { 'accept': 'application/json' }, ...COMMON_PARAMS, tags: { name: 'SignUpNewUser' } });
    const isSuccess = check(res, { 'SignUp: status is 200': (r) => r.status === 200, 'SignUp: response is a string': (r) => typeof r.json('data') === 'string' });
    if (!isSuccess) console.error(`[FAIL] SignUpNewUser. Status: ${res.status}, Body: ${res.body}`);
    return isSuccess ? res.json('data') : null;
}
function createInvitationCode(authToken) {
    const res = http.post(`${BASE_URLS.COUPLE}/invitation-code`, null, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'CreateInvitationCode' } });
    const isSuccess = check(res, { 'CreateCode: status is 200': (r) => r.status === 200 });
    if (!isSuccess) console.error(`[FAIL] CreateCode. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess ? res.json('data.invitationCode') : null;
}
function connectCouple(authToken, invitationCode) {
    const payload = JSON.stringify({ invitationCode });
    const params = { headers: { ...JSON_CONTENT_TYPE_HEADER, ...getAuthHeaders(authToken) }, ...COMMON_PARAMS, tags: { name: 'ConnectCouple' } };
    const res = http.post(`${BASE_URLS.COUPLE}/connect`, payload, params);
    const isSuccess = check(res, { 'Connect: status is 200': (r) => r.status === 200 });
    if (!isSuccess) console.error(`[FAIL] ConnectCouple. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}
function getMyCoupleInfo(authToken) {
    const res = http.get(`${BASE_URLS.COUPLE}/me`, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetMyCoupleInfo' } });
    const isSuccess = check(res, { 'GetInfo: status is 200': (r) => r.status === 200, 'GetInfo: has coupleId': (r) => r.json('data.coupleId') != null });
    if (!isSuccess) console.error(`[FAIL] GetMyInfo. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    return isSuccess;
}


export default function () {
    // finally 블록에서 접근할 수 있도록 변수를 외부에 선언합니다.
    let emailA = null;
    let emailB = null;

    // === 1. 신규 사용자 2명 생성 및 로그인 ===
    let userAToken, userBToken;
    group('1. Sign-Up and Login', function () {
        // 사용자 A
        emailA = signUpNewUser();
        if (emailA) userAToken = signInWithTestEmail(emailA, 3600)?.accessToken;
        // 사용자 B
        emailB = signUpNewUser();
        if (emailB) userBToken = signInWithTestEmail(emailB, 3600)?.accessToken;
    });

    if (!userAToken || !userBToken) {
        console.error(`[ABORT] Login failed for one or both users. A: ${emailA}, B: ${emailB}`);
        return;
    }
    sleep(1);

    // === 2. 사용자 A가 초대 코드 생성 ===
    let invitationCode = null;
    group('2. Create Invitation Code', function () {
        invitationCode = createInvitationCode(userAToken);
    });
    if (!invitationCode) {
        console.error(`[ABORT] Invitation code creation failed for user A: ${emailA}`);
        return;
    }
    sleep(1);

    // === 3. 사용자 B가 코드로 커플 연결 ===
    let connectSuccess = false;
    group('3. Connect Couple', function () {
        connectSuccess = connectCouple(userBToken, invitationCode);
    });
    if (!connectSuccess) {
        console.error(`[ABORT] Couple connection failed for user B: ${emailB}`);
        return;
    }
    sleep(1);

    // === 4. 양쪽 사용자가 커플 정보 조회 ===
    group('4. Get My Info', function () {
        getMyCoupleInfo(userAToken);
        getMyCoupleInfo(userBToken);
    });
    sleep(1);
}