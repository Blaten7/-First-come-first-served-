# 선착순 구매 프로젝트 - First come, First serve
<p id="top"></p>

## 📋 목차
- [👋 소개](#intro)
- [🛠️ 기술스택](#skills)
- [⚙️ 시스템 아키텍쳐](#structure)
  - [🏗️ 시스템 구성도](#timeline)
  - [🔄 서비스 통신 구조](#sq)
  - [🔨 서비스 구현체 구조](#impl)
  - [📦 서비스 별 기능 명세](#modules)
- [💻 실행방법](#play)
- [✨ 구현 내용](#details)
- [🤔 기술적 의사결정](#select)
- [🔍 트러블슈팅](#trouble_shooting)
- [⚡ 성능개선](#levelup)
- [📅 프로젝트 일정](#schedules)

<h2 id="intro">📌 소개 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/1.-%F0%9F%93%8C-%EC%86%8C%EA%B0%9C" style="color: green">Wiki</a><a href="#top">🔝</a></h2>

- 이커머스 플랫폼의 선착순 한정판 상품 구매 시 발생하는<br>대규모 트래픽과 동시성 이슈를 해결하기 위한 프로젝트입니다.

### 👥 개발 기간
- **인원** : 개인 프로젝트 (1인)
- **기간** : 24.12.18 ~ 25.01.09 (약 3주)

### 🔍 특징

- 동시성 제어: Redis를 활용한 대규모 트래픽 처리와 데이터 정합성 보장
- 확장 가능한 설계: MSA 아키텍처 기반으로 서비스 간 독립적 확장 가능
- 사용자 경험 최적화: 실시간 재고 관리 및 주문 상태 추적 시스템 구현

### 💻 핵심 기능
#### 회원 관리 시스템
- 회원가입, 로그인, 로그아웃 등 기본적인 사용자 인증
- 개인정보 관리 및 마이페이지 기능
- 회원 정보 기반의 주문/결제/환불 처리

#### 커머스 핵심 기능
- WishList를 통한 관심 상품 관리
- 주문 내역 조회 및 관리
- 실시간 주문 상태 추적

#### 재고 관리 시스템
- Redis를 활용한 실시간 재고 수량 관리
- 동시 주문 발생 시 재고 정합성 보장
- 품절 상태 실시간 반영

#### 주문 처리 시스템
- 대규모 트래픽 상황에서의 안정적인 주문 처리
- 분산 락을 통한 동시성 제어
- 결제 시스템 연동 및 트랜잭션 관리

<h2 id="skills">🛠️ 기술스택 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/3.-%F0%9F%9B%A0%EF%B8%8F-%EA%B8%B0%EC%88%A0%EC%8A%A4%ED%83%9D" style="color: green">Wiki</a><a href="#top">🔝</a></h2>
<table>
  <tr>
    <td>백엔드</td>
    <td>
      <img src="https://img.shields.io/badge/java 17-007396?style=for-the-badge&logo=java&logoColor=white" alt="">
      <img src="https://img.shields.io/badge/springboot 3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="">
      <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="">
<img src="https://img.shields.io/badge/spring%20data%20jpa-004225?style=for-the-badge&logo=spring&logoColor=white" alt="">
    </td>
  </tr>
  <tr>
    <td>서버</td>
    <td>
      <img src="https://img.shields.io/badge/apache tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=white" alt="">
      <img src="https://img.shields.io/badge/AWS RDS-000000?style=for-the-badge&logo=apachetomcat&logoColor=white" alt="">
    </td>
  </tr>
  <tr>
    <td>데이터베이스</td>
    <td>
        <img src="https://img.shields.io/badge/MySQL 8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="">
    </td>
  </tr>
  <tr>
    <td>분산시스템 / 아키텍쳐</td>
    <td>
        <img src="https://img.shields.io/badge/Eureka_Server-5A5A5A?style=for-the-badge&logo=spring&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/Spring_Cloud_Gateway-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/MSA-0088CC?style=for-the-badge&logo=microgen&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="">
    </td>
  </tr>
  <tr>
    <td>테스트</td>
    <td>
        <img src="https://img.shields.io/badge/K6-5563C1?style=for-the-badge&logo=k6&logoColor=white" alt="K6">
        <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit">
        <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white" alt="Postman">
        <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger">
    </td>
  </tr>
</table>
<br>

<h2 id="structure">⚙️ MSA 기반 시스템 아키텍쳐 <a href="#top">🔝</a></h2>
<img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/MSA%20Flow.png" alt="MSA Flow">
<h3 id="timeline">🏗️ 시스템 구성도</h2>

- 선착순 구매 시스템의 핵심 프로세스를 시간순으로 표현
- 각 단계별 주요 기능을 직관적인 아이콘으로 시각화
<details>
    <summary>타임라인</summary>

![시스템 개요 - 타임라인 형식](https://github.com/Blaten7/image/blob/main/images/FcomeFserve/%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EA%B0%9C%EC%9A%94%20-%20%ED%83%80%EC%9E%84%EB%9D%BC%EC%9D%B8%20%ED%98%95%EC%8B%9D.png?raw=true)
</details>

<h3 id="sq">🔄 서비스 통신 구조</h2>

- 서비스 간 상세 데이터 흐름
- 성공/실패 시나리오 포함
- 각 단계별 구체적인 상호작용 명세
<details>
    <summary>시퀀스 다이어그램</summary>

![시퀀스 다이어그램](https://github.com/Blaten7/image/blob/main/images/FcomeFserve/%EC%8B%9C%ED%80%80%EC%8A%A4%20%EB%8B%A4%EC%9D%B4%EC%96%B4%EA%B7%B8%EB%9E%A8.png?raw=true)
</details>

<h3 id="impl">🔨 서비스 구현체 구조</h2>

- 각 MSA 서비스의 내부 패키지 구조와 Eureka Server 연동 구조
<details>
    <summary>자세히보기</summary>
<h3>FcomeFeserve Project</h3>
<h4>프로젝트 구조</h4><br>
<h6>MSA(Microservice Architecture) 기반의 이커머스 서비스 프로젝트입니다.</h6><br>
<img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/MSA%20%EA%B5%AC%EC%A1%B0%EB%8F%842.png" alt="MSA 구조도">
</details>

<h3 id="modules">📦 서비스 별 기능 명세</h2>

<details>
  <summary>자세히보기</summary>

# 🔍 EurekaServer

- Spring Cloud Netflix Eureka 기반 서비스 디스커버리 서버
- 마이크로서비스 등록 및 위치 관리
- 서비스 인스턴스의 상태 모니터링

# 🌐 Gateway

- Spring Cloud Gateway 기반 API Gateway
- 라우팅, 로드밸런싱
- 공통 필터 처리 (인증/인가, 로깅 등)

# 📦 OrderService

- 주문 처리 및 관리
- 주문 상태 추적
- 주문 관련 스케줄링 작업
- 외부 서비스 연동

# 🛍️ ProductService

- 상품 정보 관리
- 상품 카탈로그 제공
- 재고 관리

# 💳 PurchaseService

- 구매 프로세스 관리
- 결제 처리
- 구매 이력 관리

# 👥 UserService

- 사용자 계정 관리
- 인증/인가 처리
- 사용자 프로필 관리
</details>

<h2 id="play">💻 실행방법 <a href="" style="color: green">Wiki</a><a href="#top">🔝</a></h2>
<h3>[ API 명세 ]</h3>
<a href="https://documenter.getpostman.com/view/38985084/2sAYJ3F2XJ">Postman API 명세서 보기</a>

<h2 id="details">✨ 구현 내용 <a href="#top">🔝</a></h2>

### 1. MSA 기반 서비스 독립성과 확장성 향상
- Eureka 서비스 디스커버리를 사용하여 서비스 간 동적 등록 및 상태 모니터링.
- Spring Cloud Gateway 를 통해 클라이언트 요청을 서비스로 라우팅하고, 인증 및 요청 검증 처리.
- 일부 모듈은 Spring WebFlux 기반으로 비동기 처리 구조를 채택하여 높은 응답 속도와 확장성을 제공.

### 2. 동적 서비스 등록 및 라우팅
- Eureka 로 각 서비스를 자동 등록 및 관리하며, 서비스 확장 및 축소가 가능한 환경 구성.
- API Gateway 를 활용하여 요청을 효율적으로 라우팅하고, 인증 및 로깅을 중앙화.

### 3. 외부 모듈 통신 및 비동기 지원
- Spring WebClient 를 사용해 비동기 HTTP 호출을 구현하며, 간결하고 확장 가능한 모듈 간 통신 제공.
- Resilience4j Circuit Breaker 의 Retry 및 Timeout 설정을 통해 네트워크 장애 시에도 안정적으로 요청을 처리.

### 4. Redis를 이용한 캐싱 처리
- Redis를 통해 실시간 데이터 캐싱을 구현하여 데이터 조회 성능 최적화.
- 동시성 문제를 해결하기 위해 Redis 의 분산 락을 일부 로직에 적용.

### 5. 컨테이너 기반 개발 및 배포 환경
- Docker Compose 를 사용하여 로컬 개발 환경 및 배포 환경에서 동일한 구성을 유지.
- 모든 서비스와 외부 의존성을 컨테이너로 구성해 일관된 개발/운영 환경 제공.

<h2 id="select">🤔 기술적 의사결정 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95" style="color: green">Wiki</a><a href="#top">🔝</a></h2>

- [1. Dockerfile과 docker-compose.yml 위치 선정 기준](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#1-Dockerfile과-docker-compose.yml-위치-선정-기준)
- [2. RESTful API 응답 코드 설계](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#2-RESTful-API-응답-코드-설계)
- [3. UserController 검증 로직 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#3-UserController-검증-로직-개선-작업)
- [4. 검증 실패 메시지 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#4-검증-실패-메시지-개선-작업)
- [5. 이메일 인증 기반 회원가입 로직 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#5-이메일-인증-기반-회원가입-로직-개선-기록)
- [6. 사용자 관리 컨트롤러 개발 목표 및 진행 기록](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#6-사용자-관리-컨트롤러-개발-목표-및-진행-기록)
- [7. ConfigServer 설정 관리 방식 결정](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#7-ConfigServer-설정-관리-방식-결정)
- [8. DB 스키마 분할](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#8-DB-스키마-분할-결정)
- [9. 주문 관리 서비스의 유저 검증 로직에 대한 고민](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#9-주문-관리-서비스의-유저-검증-로직에-대한-고민)
- [10. 공통 클래스 관리 방식 고민: 개별 관리 vs 중앙 서비스](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#10-공통-클래스-관리-방식-고민-개별-관리-vs-중앙-서비스)
- [11. 연관 관계 처리 방식 선택: 1안 vs 2안](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#11-연관-관계-처리-방식-선택-1안-vs-2안)
- [12. 선착순 구매 서비스 설계 및 구성 방향](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#12-선착순-구매-서비스-설계-및-구성-방향)
- [13. 선착순 구매 서비스(FF) 설계 방향 고민 기록](https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95#13-선착순-구매-서비스FF-설계-방향-고민-기록)

<h2 id="trouble_shooting">🔍 트러블슈팅 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/6.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85" style="color: green">Wiki</a><a href="#top">🔝</a></h2>

- [1. 로그인 검증 로직 오류 및 해결](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#1-로그인-검증-로직-오류-및-해결)
- [2. 동적 라우팅 문제 분석 및 해결 방안](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#2-동적-라우팅-문제-분석-및-해결-방안)
- [3. Redis 도커 연결 문제와 해결 방법](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#3-Redis-도커-연결-문제와-해결-방법)
- [4. WebFlux에서 WebClient 사용 이슈 및 해결 방안](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#4-WebFlux에서-WebClient-사용-이슈-및-해결-방안)
- [5. 로그인 검증 응답속도 개선 방안](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#5-로그인-검증-응답속도-개선-방안)
- [6. PurchaseService의 결제 프로세스 API 성능 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#6-PurchaseService의-결제-프로세스-API-성능-개선)
- [7. K6 테스트 코드 변경 완료 및 진행 방향](https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85#7-K6-테스트-코드-변경-완료-및-진행-방향)

<h2 id="levelup">⚡ 성능개선 <a href="" style="color: green">Wiki</a><a href="#top">🔝</a></h2>

### 주문 API 응답속도 개선
- **초기 응답속도**: 평균 2500ms (최대 5348ms)
- **개선 후 응답속도**: 평균 55ms (최대 342ms)
- **개선율**: 95% 감소

#### 주요 개선사항
1. JWT 검증 로직 게이트웨이 이전
2. 상품 정보 일괄 조회로 중복 요청 제거
3. 주문 검증 로직 병렬 처리

#### Before vs After
```java
// Before: 순차적 검증 및 처리
JWT 검증 → 상품 검증 → 재고 확인 → 주문 처리
(2500ms)

// After: 최적화된 처리
게이트웨이 JWT 검증 → 상품/재고 동시 검증 → 주문 처리
(55ms)
```

- [1. 회원 관리 서비스 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/6.-%E2%9A%A1%EC%84%B1%EB%8A%A5%EA%B0%9C%EC%84%A0#1-회원-관리-서비스-개선)
- [2. 로그인 검증 응답속도 개선 방안](https://github.com/Blaten7/-First-come-first-served-/wiki/6.-%E2%9A%A1%EC%84%B1%EB%8A%A5%EA%B0%9C%EC%84%A0#2-로그인-검증-응답속도-개선)
- [3. 주문 API 응답속도 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/6.-%E2%9A%A1%EC%84%B1%EB%8A%A5%EA%B0%9C%EC%84%A0#3-주문-API-응답속도-개선)
- [4. 결제 프로세스 API 성능 개선](https://github.com/Blaten7/-First-come-first-served-/wiki/6.-%E2%9A%A1%EC%84%B1%EB%8A%A5%EA%B0%9C%EC%84%A0#4-결제-프로세스-API-성능-개선-1)

  <h2 id="schedules">📆 프로젝트 일정 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/7.-%F0%9F%93%86-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%9D%BC%EC%A0%95" style="color: green">Wiki</a><a href="#top">🔝</a></h2>

