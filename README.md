# 선착순 구매 프로젝트 - First come, First serve
***
<p id="top"></p>

## 📋 목차
***
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

<h2 id="intro">📌 소개 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/1.-%F0%9F%93%8C-%EC%86%8C%EA%B0%9C" style="color: green">WIKI</a><a href="#top">🔝</a></h2>

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

<h2 id="skills">🛠️ 기술스택 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/3.-%F0%9F%9B%A0%EF%B8%8F-%EA%B8%B0%EC%88%A0%EC%8A%A4%ED%83%9D" style="color: green">WIKI</a><a href="#top">🔝</a></h2>
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
    <td>테스트 도구</td>
    <td>
        <img src="https://img.shields.io/badge/K6-5563C1?style=for-the-badge&logo=k6&logoColor=white" alt="K6">
        <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white" alt="Postman">
        <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit">
    </td>
  </tr>
  <tr>
    <td>형상관리</td>
    <td>
        <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white" alt="">
    </td>
  </tr>
  <tr>
    <td>IDE</td>
    <td>
      <img src="https://img.shields.io/badge/intelliJ IDEA-000000?style=for-the-badge&logo=intelliJ IDEA&logoColor=white" alt="">
    </td>
  </tr>
</table>
<br>

<h2 id="structure">⚙️ 시스템 아키텍쳐 <a href="#top">🔝</a></h2>

***
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

<h2 id="play">💻 실행방법 <a href="" style="color: green">WIKI</a><a href="#top">🔝</a></h2>

***
<h3>[ API 명세 ]</h3>
<a href="https://documenter.getpostman.com/view/38985084/2sAYJ3F2XJ">Postman API 명세서 보기</a>

<h2 id="details">✨ 구현 내용 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/4.-%E2%9C%A8-%EA%B5%AC%ED%98%84-%EB%82%B4%EC%9A%A9" style="color: green">WIKI</a><a href="#top">🔝</a></h2>
***

### 1. MSA 기반 서비스 독립성과 확장성 향상

### 2. 동적 서비스 등록 및 라우팅

### 3. 외부 모듈 통신 및 비동기 지원

### 4. Redis를 이용한 캐싱 처리

### 5. 컨테이너 기반 개발 및 배포 환경

<h2 id="select">🤔 기술적 의사결정 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/5.-%F0%9F%A4%94-%EA%B8%B0%EC%88%A0%EC%A0%81-%EC%9D%98%EC%82%AC%EA%B2%B0%EC%A0%95" style="color: green">WIKI</a><a href="#top">🔝</a></h2>

***
### 💡 선착순 구매 서비스 독립 설계
- **높은 트래픽 처리**: WebFlux 기반 리액티브 프로그래밍으로 대규모 동시 요청 처리
- **서비스 격리**: 외부 서비스 장애로부터 PurchaseService 보호
- **성능 최적화**: 인증/인가 내재화로 외부 의존성 최소화

### ⚡ 주요 기술 스택 결정
- **WebFlux**: 논블로킹 동시성 처리
- **Redis**: 재고 관리 및 사용자 요청 제어
- **JWT**: 내부 인증 처리로 외부 서비스 의존성 제거

<h2 id="trouble_shooting">🔍 트러블슈팅 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/6.-%F0%9F%94%8D-%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85" style="color: green">WIKI</a><a href="#top">🔝</a></h2>
***
### WebClient 비동기 처리 이슈

#### 문제 상황
- WebClient 사용 시 `.block()`으로 동기 방식 전환
- 동일 스레드에서 실행되어 결과값 미인식 및 에러 발생

#### 해결 방안
```java
public CompletableFuture<Boolean> sendRequestAsync(String url) {
   return WebClient.create()
       .get()
       .uri(url)
       .retrieve()
       .bodyToMono(Boolean.class)
       .subscribeOn(Schedulers.boundedElastic()) 
       .toFuture();
}
```
- WebClient 요청을 비동기 방식으로 전환
- `Schedulers.boundedElastic()`을 사용해 별도 스레드에서 작업 실행 

상세 내용 : 링크

<h2 id="levelup">⚡ 성능개선 <a href="" style="color: green">WIKI</a><a href="#top">🔝</a></h2>

***
### 1. 로그인 검증 응답속도 개선
- **기존 방식**: 각 서비스에서 개별적으로 User Service 호출 (평균 17ms)
- **개선 방식**: Gateway 레벨에서 통합 검증 처리 (평균 7ms)

#### 개선 효과
- 응답속도 평균 10ms 감소
- 서비스 간 중복 호출 제거
- 검증 로직 중앙화로 유지보수성 향상

### 2. 주문 API 응답속도 개선
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
<h2 id="schedules">📆 프로젝트 일정 <a href="https://github.com/Blaten7/-First-come-first-served-/wiki/7.-%F0%9F%93%86-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%9D%BC%EC%A0%95" style="color: green">WIKI</a><a href="#top">🔝</a></h2>

