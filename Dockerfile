# Hanacard 서버용 Dockerfile
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build.gradle 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# Gradle 빌드 실행
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# 기존 JAR 파일 삭제 후 새 JAR 파일을 app.jar로 복사
RUN rm -f app.jar && find build/libs -name "*.jar" -type f -exec cp {} app.jar \;

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
