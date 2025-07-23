import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';
import { signInWithTestEmail } from '../utils/auth.js';
import {BASE_URLS, COMMON_PARAMS, getAuthHeaders, JSON_CONTENT_TYPE_HEADER} from '../utils/common.js';

const STAGES = [
    { duration: '2m', target: 200 },
    { duration: '1m', target: 200 },
    { duration: '2m', target:   0 },
];

export const options = {
    stages: STAGES,
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<500'],
        'group_duration{group:::Fetch Today Game}': ['p(95)<500'],
        'group_duration{group:::Vote on Game}': ['p(95)<500'],
    },
};

export function setup() {
    console.log('== Setup phase: Pre-authenticating users... ==');
    const maxVUs = STAGES
        .map((s) => Number(s.target))
        .reduce((max, v) => Math.max(max, v), 0);
    const users = [];
    const tokenExpSeconds = 3600;

    for (let i = 1; i <= maxVUs; i++) {
        const email = `user${i}@ex.com`;
        const tokens = signInWithTestEmail(email, tokenExpSeconds, `device-couple-${i}`);

        if (tokens && tokens.accessToken) {
            users.push({ email, accessToken: tokens.accessToken });
        } else {
            throw new Error(`[FATAL] Failed to sign in user: ${email} during setup. This user should exist and be in a couple.`);
        }
    }
    console.log(`== Setup complete: ${users.length} coupled users authenticated. ==`);
    return users;
}

/**
 * 오늘의 밸런스 게임을 조회하고, 게임 정보를 반환합니다.
 * @returns {object|null} 게임 정보 객체 또는 null
 */
function getTodayBalanceGame(authToken) {
    const res  = http.get(`${BASE_URLS.BALANCE_GAME}/today`, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetTodayGame' } });

    let gameData = null;
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has gameInfo with options': (r) => {
            const gameInfo = r.json('data.gameInfo');
            if (gameInfo && Array.isArray(gameInfo.options) && gameInfo.options.length > 0) {
                gameData = gameInfo; // check 함수 내에서 데이터 할당
                return true;
            }
            return false;
        },
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetTodayBalanceGame. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    }
    return gameData;
}

/**
 * 밸런스 게임에 투표하고 성공 여부를 반환합니다.
 * @returns {boolean} 성공 여부
 */
function selectBalanceGameOption(authToken, gameId, optionId) {
    const url = `${BASE_URLS.BALANCE_GAME}/${gameId}`;
    const payload = JSON.stringify({ optionId });
    const res = http.post(url, payload, {
        headers: {
            ...JSON_CONTENT_TYPE_HEADER,
            ...getAuthHeaders(authToken),
        },
        tags: { name: 'VoteOnGame' }
    });

    const isSuccess = check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'response has myChoice with correct id': (r) => r.json('data.myChoice.id') === optionId,
    });

    if (!isSuccess) {
        console.error(`[FAIL] VoteOnGame. VU: ${__VU}, GameID: ${gameId}, OptionID: ${optionId}, Status: ${res.status}, Body: ${res.body}`);
    }
    return isSuccess;
}

export default function (users) {
    const currentUser = users[exec.vu.idInTest - 1];
    if (!currentUser) { return; }

    const authToken = currentUser.accessToken;
    let game = null;

    // 1. 오늘의 밸런스 게임 조회
    group('Fetch Today Game', function () {
        game = getTodayBalanceGame(authToken);
    });

    sleep(1);

    // 2. 게임 정보가 있을 경우에만 투표 진행
    if (game) {
        // VU 번호에 따라 다른 옵션을 선택
        // 이미 DB에 선택된 데이터가 있다면, optionId에 따라 실패할 수 있음
        const chosenOption = game.options[exec.vu.idInTest % game.options.length];

        group('Vote on Game', function () {
            selectBalanceGameOption(authToken, game.id, chosenOption.id);
        });
    }

    sleep(1);
}