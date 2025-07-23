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
        'http_req_duration': ['p(95)<500'],
        'group_duration{group:::Get My Couple Info}': ['p(95)<500'],
        'group_duration{group:::Get Couple Detail}': ['p(95)<600'],
    },
};

export function setup() {
    console.log('== Setup phase: Pre-authenticating users known to be in a couple... ==');
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
 * 내 커플 정보를 조회하고, 커플 ID를 반환합니다.
 * @returns {number|null} 커플 ID 또는 null
 */
function getMyCoupleInfo(authToken) {
    const url = `${BASE_URLS.COUPLE}/me`;
    const res = http.get(url, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetMyCoupleInfo' } });

    let coupleId = null;
    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has data.coupleId': (r) => {
            const id = r.json('data.coupleId');
            if (id != null) {
                coupleId = id; // check 함수 내에서 변수 할당
                return true;
            }
            return false;
        },
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetMyCoupleInfo. VU: ${__VU}, Status: ${res.status}, Body: ${res.body}`);
    }
    return coupleId;
}

/**
 * 특정 커플 ID의 상세 정보를 조회하고 검증합니다.
 */
function fetchCoupleDetail(authToken, coupleId) {
    const url = `${BASE_URLS.COUPLE}/${coupleId}`;
    const res = http.get(url, { headers: getAuthHeaders(authToken), ...COMMON_PARAMS, tags: { name: 'GetCoupleDetail' } });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'response has correct coupleId': (r) => r.json('data.coupleId') == coupleId,
        'response has myInfo': (r) => r.json('data.myInfo') != null,
        'response has partnerInfo': (r) => r.json('data.partnerInfo') != null,
    });

    if (!isSuccess) {
        console.error(`[FAIL] GetCoupleDetail. VU: ${__VU}, ID: ${coupleId}, Status: ${res.status}, Body: ${res.body}`);
    }
}

export default function (users) {
    const currentUser = users[exec.vu.idInTest - 1];
    if (!currentUser) { return; }

    const authToken = currentUser.accessToken;
    let coupleId = null;

    // 1. 내 커플 정보 조회 시나리오
    group('Get My Couple Info', function () {
        coupleId = getMyCoupleInfo(authToken);
    });

    sleep(1);

    // 2. 커플 상세 정보 조회 시나리오 (coupleId가 있을 때만 실행)
    if (coupleId) {
        group('Get Couple Detail', function () {
            fetchCoupleDetail(authToken, coupleId);
        });
    }

    sleep(1);
}