## 유저 테이블에 추가 배치 수행
./gradlew bootRun --args='--spring.batch.job.name=addJob'

## FCM 전송 및 성공시 제거 배치 수행
./gradlew bootRun --args='--spring.batch.job.name=anniversaryJob'

