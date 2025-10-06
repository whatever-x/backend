## 유저 테이블에 추가 배치 수행
./gradlew notificationAddBatch

## FCM 전송 및 성공시 제거 배치 수행
./gradlew anniversaryBatch

// 중복 처리 하기 (중복으로 add 되는중 - DB 적으로 분리가 될 수 있는지)
// 커넥션이 분리가 된건지 확인
// 테스트 코드... 가능하면 짜기
