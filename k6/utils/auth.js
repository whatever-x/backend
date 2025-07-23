import http from 'k6/http';
import { check } from 'k6';
import { BASE_URLS } from './common.js';

const TEST_LOGIN_API_HOST = BASE_URLS.SAMPLE;
const DEFAULT_TOKEN_EXP_SECONDS = 3600;

/**
 * 테스트 이메일로 로그인하여 토큰을 발급받습니다.
 * @param {string} email - 테스트 사용자 이메일.
 * @param {number} [expSeconds=DEFAULT_TOKEN_EXP_SECONDS] - 토큰 만료 시간(초).
 * @param {string} [deviceId] - 디바이스 ID.
 * @returns {object|null} 성공 시 { accessToken, refreshToken }, 실패 시 null.
 */
export function signInWithTestEmail(email, expSeconds = DEFAULT_TOKEN_EXP_SECONDS, deviceId) {
    const url = `${TEST_LOGIN_API_HOST}/test/sign-in?email=${encodeURIComponent(email)}&expSec=${expSeconds}`;

    const headers = {};
    if (deviceId) {
        headers['Device-Id'] = deviceId;
    }
    const params = { headers, timeout: '60s' };

    // 디버그 모드가 켜져 있을 때만 상세 로그를 출력합니다. (k6 run -e K6_DEBUG=true ...)
    if (__ENV.K6_DEBUG) {
        console.log(`[AUTH] Attempting test sign-in for email: ${email}`);
    }

    const res = http.get(url, params);

    const jsonData = res.json();
    const tokens = jsonData && jsonData.data ? jsonData.data.serviceTokenResponse : null;

    const isSuccess = check(res, {
        'testSignIn: status is 200': (r) => r.status === 200,
        'testSignIn: response has valid token structure': () =>
            tokens && typeof tokens.accessToken === 'string' && typeof tokens.refreshToken === 'string',
    });

    if (isSuccess) {
        if (__ENV.K6_DEBUG) {
            console.log(`[AUTH] Test sign-in successful for email: ${email}.`);
        }
        return tokens;
    } else {
        console.error(
            `[AUTH] Test sign-in failed. Email: ${email}, Status: ${res.status}, Body: ${res.body}`
        );
        return null;
    }
}