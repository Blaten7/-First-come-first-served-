# 1. 베이스 이미지 설정
FROM openjdk:17-jdk-slim

# 2. 애플리케이션 유지보수자 설정
LABEL maintainer="user"

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 빌드된 JAR 파일을 복사하면서 이름 변경
COPY build/libs/infra.jar /app/infra.jar

# 5. 실행 환경 변수 설정 (선택 사항)
ENV SERVER_PORT=8080

# 6. 컨테이너 실행 명령어 설정
ENTRYPOINT ["java", "-jar", "/app/infra.jar"]
