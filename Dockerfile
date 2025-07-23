# 1. 베이스 이미지 선택
FROM amazoncorretto:21

# 2. JAR 파일을 컨테이너 내부로 복사
ARG JAR_FILE=caramel-api/build/libs/whatever-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 3. JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
