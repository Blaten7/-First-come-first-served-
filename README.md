# 선착순 구매 프로젝트 - First come, First serve
***
<p id="top"></p>

## 📋 목차
***
- [👋 소개](#소개)             
- [🎯 기획](#idea)            
- [🛠️ 기술스택](#skills)
- [🏗️ 프로젝트 구조](#structure)
    - [⚙️ 시스템 아키텍쳐](#modules)
    - [🔄 전체 MSA 구조도](#MSA)
    - [📦 세부 모듈 설명](#modules)
- [💻 실행방법](#play)
- [✨ 구현 내용](#details)
- [🤔 기술적 의사결정](#select)
- [🔍 트러블슈팅](#trouble_shooting)
- [⚡ 성능개선](#levelUp)
- [📅 프로젝트 일정](#schedules)
- [🎉 결과](#result)

## 📌 소개
***
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

## 🎯 기획 [🔝](#top)
***
### 요구사항 분석

#### 기능적 요구사항

- 사용자는 실시간으로 상품 구매가 가능해야 한다.
- 선착순 구매 성공 여부를 즉시 확인할 수 있어야 한다.
- 관리자는 상품의 재고를 실시간으로 조회 및 수정할 수 있어야 한다.
- 
#### 비기능적 요구사항

- 구매 요청은 200ms 이내에 응답해야 한다.
- 시스템 가용성은 99.9% 이상을 유지해야 한다.
- 데이터는 DB와 캐시를 활용하여 안전하게 관리해야 한다.

<details>
    <summary>📊 ERD 구상</summary>
  <img src="https://github.com/Blaten7/image/blob/main/images/FcomeFserve/ERD_1차.png?raw=true" alt="">
<h3>📝 초기 데이터 설계</h3>

1. **사용자(User) 정보 관리**
- 기본적인 회원 정보(이름, 이메일, 비밀번호) 외에도 배송을 위한 주소, 연락처 정보 포함
- 프로필 이미지와 자기소개 등 부가적인 사용자 정보도 고려
- 비밀번호 변경 이력 관리를 위한 pwUpdatedAt 필드 추가
- 회원 탈퇴 등을 고려하여 생성일(createdAt) 기록

2. **상품(Product) 관리**
- 상품의 기본 정보(이름, 설명, 가격) 관리
- 재고 수량(stockQuantity)을 통한 재고 관리
- 상품의 등록일(createdAt)과 수정일(updatedAt)을 통한 이력 관리
- 가격은 소수점 연산을 고려하여 decimal(10,2) 타입 선택

3. **주문(Order) 시스템**
- 주문번호(orderNum)를 통한 주문 식별
- 주문 상태(orderStatus)를 통한 주문 진행 상황 관리
- 총 주문금액(totalAmount) 별도 관리
- 주문 시각(orderDate) 기록

4. **주문상품(OrderItem) 관리**
- 하나의 주문에 여러 상품을 담을 수 있도록 설계
- Order와 Product를 연결하는 중간 테이블 역할
- 외래키를 통한 관계 설정으로 데이터 정합성 보장

5. **위시리스트(Wishlist) 기능**
- 사용자가 관심 있는 상품을 저장할 수 있는 기능
- 사용자와 상품 간의 다대다 관계 해소
- 수량(quantity) 필드를 통해 향후 장바구니 기능으로의 확장 고려
- 생성 시각(createdAt) 기록으로 위시리스트 추가 이력 관리

### 향후 고려사항
- 상품의 카테고리 분류 체계 추가 필요
- 주문 취소/환불 처리를 위한 상태값 확장
- 결제 정보 연동을 위한 테이블 추가 검토
- 상품 이미지 관리를 위한 별도 테이블 고려
</details>

<h2 id="skills">🛠️ 기술스택 <a href="#top">🔝</a></h2>
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

## ⚙️ 시스템 아키텍쳐[🔝](#top)
***
### 🏗️ 시스템 구성도
- 선착순 구매 시스템의 핵심 프로세스를 시간순으로 표현
- 각 단계별 주요 기능을 직관적인 아이콘으로 시각화
<details>
    <summary>타임라인</summary>

![시스템 개요 - 타임라인 형식](https://github.com/Blaten7/image/blob/main/images/FcomeFserve/%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EA%B0%9C%EC%9A%94%20-%20%ED%83%80%EC%9E%84%EB%9D%BC%EC%9D%B8%20%ED%98%95%EC%8B%9D.png?raw=true)
</details>

### 🔄 서비스 통신 구조
- 서비스 간 상세 데이터 흐름
- 성공/실패 시나리오 포함
- 각 단계별 구체적인 상호작용 명세
<details>
    <summary>시퀀스 다이어그램</summary>

![시퀀스 다이어그램](https://github.com/Blaten7/image/blob/main/images/FcomeFserve/%EC%8B%9C%ED%80%80%EC%8A%A4%20%EB%8B%A4%EC%9D%B4%EC%96%B4%EA%B7%B8%EB%9E%A8.png?raw=true)
</details>

### 🔨 서비스 구현체 구조
- 각 MSA 서비스의 내부 패키지 구조와 Eureka Server 연동 구조
<details>
    <summary>자세히보기</summary>
<h3>FcomeFeserve Project</h3>
<h4>프로젝트 구조</h4><br>
<h6>MSA(Microservice Architecture) 기반의 이커머스 서비스 프로젝트입니다.</h6><br>
<img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/MSA%20%EA%B5%AC%EC%A1%B0%EB%8F%842.png" alt="MSA 구조도">
</details>

### 📦 서비스 별 기능 명세
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

## 💻 실행방법[🔝](#top)
***
<h3>[ API 명세 ]</h3>
<a href="https://documenter.getpostman.com/view/38985084/2sAYJ3F2XJ">Postman API 명세서 보기</a>

## ✨ 구현 내용[🔝](#top)
***

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

## 🤔 기술적 의사결정[🔝](#top)
***
### 💡 선착순 구매 서비스 독립 설계
- **높은 트래픽 처리**: WebFlux 기반 리액티브 프로그래밍으로 대규모 동시 요청 처리
- **서비스 격리**: 외부 서비스 장애로부터 PurchaseService 보호
- **성능 최적화**: 인증/인가 내재화로 외부 의존성 최소화

### ⚡ 주요 기술 스택 결정
- **WebFlux**: 논블로킹 동시성 처리
- **Redis**: 재고 관리 및 사용자 요청 제어
- **JWT**: 내부 인증 처리로 외부 서비스 의존성 제거

상세 내용 : 링크

## 🔍 트러블슈팅[🔝](#top)
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

## ⚡ 성능개선[🔝](#top)
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
- **개선 후 응답속도**: 평균 120ms (최대 342ms)
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
(120ms)
```
## 📆 프로젝트 일정[🔝](#top)
***
<details>
    <summary>계획 및 실천</summary>
    <table>
        <tr>
            <th>/</th>
            <td>목표</td>
            <td>실천</td>
        </tr>
        <tr>
            <th colspan="3">-  -  -  -  -  1주차  -  -  -  -  -</th>
        </tr>
        <tr>
            <th>24.12.18 [수]</th>
            <td>[ 프로젝트 시작일 ]<br>
                ERD 작성<br>
                API 명세서 작성<br>
                유저 관리 서비스 전반 기능 구현
            </td>
            <td>
                ERD 초안 작성<br>
                API 명세서 작성<br>
                DDD구조 프로젝트 생성<br>
                Docker 환경설정<br>
                회원가입 기능 구현률 50%
            </td>
        </tr>
        <tr>
            <th>24.12.19 [목]</th>
            <td>
                유저 관리 서비스 기능 구현
            </td>
            <td>
                회원가입 기능구현 완료<br>
                이메일 인증 기능구현 완료<br>
                로그인 기능구현 완료<br>
                현재 기기에서 로그아웃 기능구현 완료<br>
                모든 기기에서 로그아웃 기능구현 완료<br>
                비밀번호 변경 기능구현 완료<br>
            </td>
        </tr>
        <tr>
            <th>24.12.20 [금]</th>
            <td>
                기능 구현 작업 중단<br>
                유레카 서버 구축<br>
                API 게이트웨이 구축<br>
                멀티 모듈 프로젝트에 맞는 도커환경 구축<br>
                그래들 의존성 중앙 관리식 일부 자동화 구축<br>
            </td>
            <td>
                유레카와 게이트웨이 활용을 위해서<br>
                먼저 프로젝트의 구조를 리팩토링<br>
                최초 프로젝트 내 모듈 4개에서<br>
                ConfigServer, EurekaServer, Gateway, Service...<br>
                로 구성을 하고, 각 서비스마다 서브모듈을 4개씩 구성<br><br>
                ConfigServer 구축 완료<br>
                EurekaServer 구축 완료<br>
                API Gateway 구축 완료<br>
                유저 관리 서비스 유레카에 등록 후 요청 처리 테스트 완료<br>
            </td>
        </tr>
        <tr>
            <th>24.12.21 [토]</th>
            <td>
                각 서비스 별 스키마 분할<br>
                API 명세서 보완하여 재작성<br>
                명세서 기반 상품관리 서비스 구현<br>
                -----------------Optional-----------------<br>
                주문관리 서비스 구현<br>
                Resilence4j 활용<br>
                장애상황 연출 및 회복탄력성 갖추기
            </td>
            <td>
                서비스 별 스키마 분할 완료<br>
                상품관리 서비스 구현 완료<br>
                주문관리 서비스 구현 중...<br>
                현재 각 서비스별 하위모듈이 개별적으로 동작하지 않는데도<br>
                이 구조를 유지할 이유가 없다는걸 깨달았음. <br>
                따라서 루트의 모듈 구성은 그대로 두되. <br>
                각 서비스별 하위모듈 삭제하고 하나의 구조로 리팩토링 완료
            </td>
        </tr>
        <tr>
            <th>24.12.22 [일]</th>
            <td>
                주문관리 서비스 구현<br>
                모든 서비스의 예외처리 추가<br>
                -----------------Optional-----------------<br>
                테스트 코드 및 시나리오 작성<br>
                테스트 수행 및 성능 개선<br>
            </td>
            <td>
                주문관리 서비스 구현완료    
            </td>
          </tr>
        <tr>
            <th>24.12.23 [월]</th>
            <td>
                위시리스트 API 구현완료<br>
                모든 서비스 예외처리 추가
            </td>
            <td>
                위시리스트 API 구현<br>
                모든 서비스 예외처리 추가
            </td>
        </tr>
        <tr>
            <th>24.12.24 [화]</th>
            <td>
                테스트 시나리오 및 코드 작성<br>
                테스트 후 예외처리 추가 및 성능개선
            </td>
            <td>
                서비스 커버리지 테스트 66% 달성<br>
                k6 테스트 수행, 30%의 오류율 발생 확인
            </td>
        </tr>
        <tr>
            <th colspan="3">-  -  -  -  -  2주차  -  -  -  -  -</th>
        </tr>
        <tr>
            <th>24.12.25 [수]</th>
            <td>
                Resilience4j 활용, 회복탄력성 갖추기<br>
            </td>
            <td>
                실패
            </td>
        </tr>
        <tr>
            <th>24.12.26 [목]</th>
            <td>
                동시성 제어 및 성능개선
            </td>
            <td>
                Spring Security 추가 작업중<br>
                기존 방법으로 처리가 되지 않는 에러 발생 <br>
                WebMvc -> WebFlux 로 로직 구성 변경.<br>
                유저서비스 변경 완료. 27일 나머지 서비스 변경 예정
            </td>
        </tr>
        <tr>
            <th>24.12.27 [금]</th>
            <td>
                상품관리, 주문관리 서비스 WebFlux 구조 변경<br>
                레디스 기반 분산락 구현으로 동시성 제어 Up<br>
            </td>
            <td>
                WebFlux 관련 피드백 수용<br>
                다시 WebMVC로 롤백..
            </td>
        </tr>
        <tr>
            <th>24.12.28 [토]</th>
            <td>
                회복 탄력성 공부
            </td>
            <td>
                .
            </td>
        </tr>
        <tr>
            <th>24.12.29 [일]</th>
            <td>
                회복 탄력성 공부
            </td>
            <td>
                Resilience4j 활용<br>
                Circuit Breaker, Retry, TimeLimiter 적용<br>
                Docker 환경 세팅 및 빌드 그리고 API 테스트 완료<br>
                프로메테우스 설정 완료
            </td>
        </tr>
        <tr>
            <th>24.12.30 [월]</th>
            <td>
                3주차 목표 설정<br>
                동시성 제어에 관한 공부<br>
                가능하다면 일부 적용까지
            </td>
            <td>
                코드 변경사항 발생시, 해당 모듈 재빌드,<br>
                도커에도 이미지 재빌드를 하는 과정이 번거로워서<br>
                코드 변경을 감지하여 자동으로 도커에 재빌드 된 jar파일이 빌드되도록<br>
                구성하고자 Devtools와 도커의 Volume 설정을 사용.<br>
                하지만, 뜻대로 되지 않았고. 나 혼자, 협업없이, 다른 컴퓨터에서 실행하지 않고<br>
                진행하는 프로젝트 이기 때문에 도커를 사용할 의미가 사실 없음.<br>
                다만, 도커를 사용할 수 있다는 것은 확인이 되었으니, 오늘부로 도커 아웃.
            </td>
        </tr>
        <tr>
            <th>24.12.31 [화]</th>
            <td>
                남은 재고 파악 API 설계 및 구현<br>
                Redis 캐싱에 대한 이해<br>
                결제 진입 및 결제 API 설계 및 구현
            </td>
            <td>
                공부
            </td>
        </tr>
        <tr>
            <th colspan="3">-  -  -  -  -  3주차  -  -  -  -  -</th>
        </tr>
        <tr>
            <th>25.01.01 [수]</th>
            <td>
                공부
            </td>
            <td>
                공부
            </td>
        </tr>
        <tr>
            <th>25.01.02 [목]</th>
            <td>
                .
            </td>
            <td>
                인증 / 인가 위치 변경<br>
                UserService -> Gateway<br>
                전체 서비스에서 로그인 검증을 하지 않게 되어<br>
                전체적으로 응답속도 10ms이상 향상
            </td>
        </tr>
        <tr>
            <th>25.01.03 [금]</th>
            <td>
                선착순 구매 서비스 API MVP 개발로 기능구현 완료<br>
                각 모듈간 요청에 적절한 카프카 로직 추가<br>
                모듈별 응답속도 개선<br>
            </td>
            <td>
                주문 관련 API 성능개선<br>
                평균 응답속도 2500ms -> 120ms<br>
                약 93 ~ 95% 개선<br>
                결제 프로레스 API 구현 완료<br>
                위의 API K6 커스텀 매트릭&핸들러 테스트코드 작성<br>
                VU 50 이하시에만 정상작동, 50초과시 에러율 급증
            </td>
        </tr>
        <tr>
            <th>25.01.04 [토]</th>
            <td>
                결제 프로세스 API의 동시성 제어 로직 추가<br>
                K6기준 VU 10000에서 안정적인 동작을 목표
            </td>
            <td>
                레디스 기반 분산 락을 컨트롤러단에서만 구현<br>
                결과 : VU 1000명에서 안정적인 동작 확인<br>
                데이터 정합성과 동시성을 더 확실히 제어해야할 필요
            </td>
        </tr>
        <tr>
            <th>25.01.05 [일]</th>
            <td>
                .
            </td>
            <td>
                .
            </td>
        </tr>
        <tr>
            <th>25.01.06 [월]</th>
            <td>
                <details>
                    <summary>Redis 최적화 방안</summary>
                    레디스 QPS, 응답시간 및 메세지 전달 성공률 측정<br>
                    병목 구간 파악 및 해결<br>
                    성능 최적화 이후 다시 테스트 진행
                </details>
                이후 카프카 도입 검토
            </td>
            <td>
                현재 프로젝트 진행 방향에 대해 멘토님과 상의한 결과<br>
                아쉬운 점이 있는 것 같아, 수립한 계획 전면 취소<br>
                단기 목표로는<br>
                내일 7일까지, 각 서비스별 테스트코드 작성 및 테스트 커버리지 90%이상 달성<br>
                장기 목표로는 레디스 캐싱에 대한 깊은 이해를 바탕으로 동시성 제어 성능 개선<br>
                차후 시간적 여유가 있다는 전제하에 카프카 등 선택사항 구현
            </td>
        </tr>
        <tr>
            <th>25.01.07 [화]</th>
            <td>
                오늘부터 각 서비스별 테스트코드 작성<br>
                및 테스트 커버리지 100% 달성
            </td>
            <td>
                <details>
                    <summary>UserService Test Coverage 100% 이미지</summary>
                    <img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/UserService/UserService%20-%20test%20coverage%20100%25.png" alt="테스트 커버리지 100%">
                </details>
            </td>
        </tr>
        <tr>
            <th>25.01.08 [수]</th>
            <td>
                .
            </td>
            <td>
                <details>
                    <summary>ProductService Test Coverage 100% 이미지</summary>
                    <img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/ProductService/ProductService%20-%20test%20coverage%20100%25.png" alt="테스트 커버리지 100%">
                </details>
                <details>
                    <summary>EurekaServer Test Coverage 100% 이미지</summary>
                    <img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/EurekaServer/EurekaServer%20-%20test%20coverage%20100%25.png" alt="테스트 커버리지 100%">
                </details>
                <details>
                    <summary>Gateway Test Coverage 100% 이미지</summary>
                    <img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/Gateway/Gateway%20-%20test%20coverage%20100%25.png" alt="테스트 커버리지 100%">
                </details>
            </td>
        </tr>
        <tr>
            <th>25.01.09 [목]</th>
            <td>
                .
            </td>
            <td>
                <details>
                    <summary>OrderService Test Coverage 100% 이미지</summary>
                    <img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/OrderService/OrderService%20-%20test%20coverage%20100%25.png" alt="테스트 커버리지 100%">
                </details>
                <details>
                    <summary>PurchaseService Test Coverage 90% 이미지</summary>
                    <img src="https://raw.githubusercontent.com/Blaten7/image/main/images/FcomeFserve/PurchaseService/PurchaseService%20-%20test%20coverage%2090%25.png" alt="테스트 커버리지 90%">
                </details><br>
                전체 모듈 테스트코드 작성 1차 완료        
            </td>
        </tr>
        <tr>
            <th colspan="3">-  -  -  -  -  프로젝트 종료  -  -  -  -  -</th>
        </tr>
      </table>
</details>

## 🎉 결과[🔝](#top)
***
## 📊 Results

### 🎯 목표 달성
- **동시성 제어**: Redis를 활용한 대규모 트래픽(초당 1000건) 처리 달성
- **성능 최적화**: API 응답속도 약 98% 개선 (2500ms → 55ms)
- **안정성 확보**: 분산 환경에서의 데이터 정합성 보장

### 🔍 테스트 결과
```java
// K6 부하 테스트 결과
동시 접속자: 5000명
테스트 시간: 5분
성공률: 55.8&
        → 결제중 이탈 20% X 결제 시도 중 이탈 20% 예외처리 반영
        → 기대치 55 ~ 75%
평균 응답시간: 20ms
95퍼센타일 응답 시간: 56ms
```

