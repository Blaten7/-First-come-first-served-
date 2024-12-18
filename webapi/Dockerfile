# 1. 베이스 이미지 설정
FROM openjdk:17-jdk-slim

# 2. 애플리케이션 유지보수자 설정
LABEL maintainer="user"

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 빌드된 JAR 파일을 복사하면서 이름 변경
COPY build/libs/api.jar /app/api.jar

# 5. 실행 환경 변수 설정
ENV SERVER_PORT=${WEBAPI_PORT}

# 6. 헬스체크 스크립트 복사
COPY healthcheck.sh /app/healthcheck.sh
RUN chmod +x /app/healthcheck.sh

# 7. 컨테이너 실행 명령어 설정
ENTRYPOINT ["java", "-jar", "/app/api.jar"]

# 8. HEALTHCHECK 설정
HEALTHCHECK CMD /app/healthcheck.sh
