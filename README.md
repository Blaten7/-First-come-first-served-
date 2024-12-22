<h1>💻 선착순 구매 프로젝트</h1>
<hr>
무신사, 29cm 등 이커머스 서비스 내에서 선착순 구매가 진행되는 상황을 기반으로 설계된 프로젝트로,<br>
Redis에 기반한 대규모 주문 처리 및 MSA 등을 경험할 수 있는 프로젝트
<h2>프로젝트 개요</h2>
* 기본적인 E-commerce 서비스를 위한 회원 플랫폼을 개발<br>
* 사용자는 플랫폼을 통해 회원가입, 로그인, 로그아웃, 마이페이지 등의<br>
&nbsp;&nbsp;기본적인 유저 관리 기능을 편리하게 이용할 수 있어야 합니다.<br>
* 커머스를 이용하기 위한 핵심 요소인 WishList, 주문내역, 주문상태 조회 등의 기능을 제공하여<br>
* 사용자가 원하는 물품의 구매 및 진행 상태를 원할하게 인지할 수 있도록 하여야 합니다.<br>
* 올바른 주문 및 결제/환불 등을 처리하기 위해선 회원의 개인정보가 저장되어야 합니다.

<h2>🛠️ 기술스택</h2>
<table>
  <tr>
    <td>백엔드</td>
    <td>
      <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white" alt="">
      <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="">
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
        <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="">
    </td>
  </tr>
  <tr>
    <td>IDE</td>
    <td>
      <img src="https://img.shields.io/badge/intelliJ IDEA-000000?style=for-the-badge&logo=intelliJ IDEA&logoColor=white" alt="">
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
    <td>분산시스템 / 아키텍쳐</td>
    <td>
        <img src="https://img.shields.io/badge/Eureka_Server-5A5A5A?style=for-the-badge&logo=spring&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/Spring_Cloud_Gateway-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/MSA-0088CC?style=for-the-badge&logo=microgen&logoColor=white" alt="">
        <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="">
</td>
  </tr>
</table>

<h2>💡 기획</h2><br>
<h3>📄 1차 기획안 - ERD, API 명세</h3>
<details>
    <summary>자세히보기</summary>
  <img src="https://github.com/Blaten7/image/blob/main/images/FcomeFserve/ERD_1차.png?raw=true" alt="">
<h3>[ API 명세 ]</h3>
<a href="https://documenter.getpostman.com/view/38985084/2sAYJ3F2XJ">Postman API 명세서 보기</a>
</details>
<br>
<h3>📆 프로젝트 일정 계획표</h3>
<details>
    <summary>자세히보기</summary>
    <table>
        <tr>
            <th>/</th>
            <td>목표</td>
            <td>실천</td>
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
- [ ] 주문관리 서비스 구현<br>
- [ ] 모든 서비스의 예외처리 추가<br>
- [ ] 테스트 코드 및 시나리오 작성<br>
- [ ] 테스트 수행 및 성능 개선<br>
              </td>
          </tr>
      </table>
</details>
