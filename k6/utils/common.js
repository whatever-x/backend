// k6/utils/common.js
// 이 파일은 모든 k6 테스트 스크립트에서 공통적으로 사용될 수 있는 변수, 설정, 헬퍼 함수들을 정의합니다.

const API_HOST = __ENV.API_HOST || 'http://127.0.0.1:8080';

export const BASE_URLS = {
    TEST_API_HOST: API_HOST,

    SAMPLE: `${API_HOST}/sample`,
    AUTH: `${API_HOST}/v1/auth`,
    USER: `${API_HOST}/v1/user`,
    COUPLE: `${API_HOST}/v1/couples`,
    CONTENT: `${API_HOST}/v1/content`,
    BALANCE_GAME: `${API_HOST}/v1/balance-game`,
    CALENDAR_HOLIDAYS: `${API_HOST}/v1/calendar`,
    CALENDAR_SCHEDULES: `${API_HOST}/v1/calendar/schedules`,
    FIREBASE: `${API_HOST}/v1/firebase`,
};

export const DEFAULT_DEVICE_ID = __ENV.DEFAULT_DEVICE_ID || 'tempDeviceId'; // 기본 디바이스 ID
export const DEFAULT_TIME_ZONE = __ENV.DEFAULT_TIME_ZONE || 'Asia/Seoul'; // 기본 타임존

export const COMMON_PARAMS = {
  timeout: '60s',
};

export const AUTH_HEADER_NAME = 'Authorization';

/**
 * 인증 토큰을 사용하여 HTTP 요청용 인증 헤더 객체를 생성합니다.
 * @param {string} accessToken - API 접근에 사용될 액세스 토큰.
 * @returns {object} HTTP 헤더 객체. 예: { 'Authorization': 'Bearer YOUR_ACCESS_TOKEN' }
 */
export function getAuthHeaders(accessToken) {
  return { [AUTH_HEADER_NAME]: `Bearer ${accessToken}` };
}

export const JSON_CONTENT_TYPE_HEADER = { 'Content-Type': 'application/json' };