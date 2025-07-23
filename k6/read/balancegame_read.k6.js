import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import { BASE_URLS, COMMON_PARAMS, getAuthHeaders } from '../utils/common.js';

// 1) 순수 JS 배열로 스테이지 정의
const STAGES = [
    { duration: '2m', target: 500 },
    { duration: '1m', target: 500 },
    { duration: '2m', target:   0 },
];

// 2) options 에는 STAGES 만 연결
export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<500'],
        'group_duration{group:::Fetch Today Balance Game}': ['p(95)<500'],
    },
};

export function setup() {
    console.log('== Setup phase: Pre-authenticating users for Balance Game test... ==');

    // [디버깅 로그 1] options가 올바르게 읽혔는지 확인
    console.log(`[DEBUG] Options stages: ${JSON.stringify(STAGES)}`);

    const maxVUs = STAGES
        .map((s) => Number(s.target))
        .reduce((max, v) => Math.max(max, v), 0);

    // [디버깅 로그 2] maxVUs가 올바르게 계산되었는지 확인
    console.log(`[DEBUG] Calculated maxVUs: ${maxVUs}`);

    const users = [];
    const tokenExpSeconds = 3600;

    // maxVUs가 0이면 더 이상 진행할 필요 없음
    if (maxVUs === 0) {
        console.error('[DEBUG] maxVUs is 0. The setup loop will not run.');
        return users;
    }

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-bg-${i}`);

        // [디버깅 로그 3] 각 로그인 시도의 결과를 확인
        console.log(`[DEBUG] Attempt ${i} for ${email}. Result: ${JSON.stringify(tokens)}`);

        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            // [디버깅 로그 4] 실패 시 에러를 던지기 전에 로그를 남겨 원인을 명확히 함
            console.error(`[FATAL] Failed to sign in user: ${email}. Aborting setup.`);
            throw new Error(`Failed to sign in user: ${email} during setup.`);
        }
    }
    console.log(`== Setup complete: ${users.length} users authenticated. ==`);
    return users;
}

/**
 * 오늘의 밸런스 게임을 조회하고 응답을 검증하는 함수.
 * @param {string} authToken - 인증 토큰
 * @returns {boolean} 요청 및 검증 성공 여부
 */
function fetchAndCheckTodayBalanceGame(authToken) {
    const res = http.get(`${BASE_URLS.BALANCE_GAME}/today`, {
        headers: getAuthHeaders(authToken),
        ...COMMON_PARAMS,
        tags: { name: 'GetTodayBalanceGame' }, // 결과 리포트에서 필터링을 위한 태그
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response is a valid JSON': (r) => {
            try {
                r.json();
                return true;
            } catch (e) {
                return false;
            }
        },
        'response has data.gameInfo.id': (r) => r.json('data.gameInfo.id') != null,
        'response has a non-empty options array': (r) => {
            const options = r.json('data.gameInfo.options');
            return Array.isArray(options) && options.length > 0;
        },
    });

    if (!isSuccess) {
        console.error(`Failed to get today's balance game. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    }

    return isSuccess;
}

export default function (users) {
    const currentUser = users[exec.vu.idInTest - 1];

    if (!currentUser) {
        return;
    }

    group('Fetch Today Balance Game', function () {
        fetchAndCheckTodayBalanceGame(currentUser.accessToken);
    });

    sleep(1);
}