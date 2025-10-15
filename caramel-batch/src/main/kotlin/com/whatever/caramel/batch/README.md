## 유저 테이블에 추가 배치 수행
./gradlew notificationAddBatch

## 중복 삽입 방지를 위해 유니크 인덱스 추가 해야함
CREATE UNIQUE INDEX uq_scheduled_notification
ON scheduled_notification(target_user_id, notification_type);

## FCM 전송 및 성공시 제거 배치 수행
./gradlew anniversaryBatch

// 테스트 코드... 가능하면 짜기
