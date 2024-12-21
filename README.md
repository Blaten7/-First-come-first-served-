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
      <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
      <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
      <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
<img src="https://img.shields.io/badge/spring%20data%20jpa-004225?style=for-the-badge&logo=spring&logoColor=white">
    </td>
  </tr>
  <tr>
    <td>서버</td>
    <td>
      <img src="https://img.shields.io/badge/apache tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=white">
      <img src="https://img.shields.io/badge/AWS RDS-000000?style=for-the-badge&logo=apachetomcat&logoColor=white">
    </td>
  </tr>
  <tr>
    <td>데이터베이스</td>
    <td>
        <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
        <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
    </td>
  </tr>
  <tr>
    <td>IDE</td>
    <td>
      <img src="https://img.shields.io/badge/intelliJ IDEA-000000?style=for-the-badge&logo=intelliJ IDEA&logoColor=white">
    </td>
  </tr>
  <tr>
    <td>형상관리</td>
    <td>
        <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
        <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
    </td>
  </tr>
  <tr>
    <td>분산시스템 / 아키텍쳐</td>
    <td>
        <img src="https://img.shields.io/badge/Eureka_Server-5A5A5A?style=for-the-badge&logo=spring&logoColor=white">
        <img src="https://img.shields.io/badge/Spring_Cloud_Gateway-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
        <img src="https://img.shields.io/badge/MSA-0088CC?style=for-the-badge&logo=microgen&logoColor=white">
        <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
</td>
  </tr>
</table>

<h2>💡 기획</h2>
<details>
    <summary>1차 기획안 - ERD, API 명세</summary>
  <img src="https://github.com/Blaten7/image/blob/main/images/FcomeFserve/ERD_1차.png?raw=true">
<h3>[ API 명세 ]</h3>

<사용자관리>

1. 회원가입 - 이메일인증

[Url] POST /api/users/signup<br>
[Desc] 이메일 인증을 통해 회원가입을 진행<br>
[Request]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"name": "홍길동",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"phoneNumber": "010-1234-5678",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"address": "서울특별시 강남구",<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"email": "user@example.com",<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"password": "password123"<br>
}<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "인증메일이 전송되었습니다. 메일을 확인해주세요."<br>
}<br>

2. 이메일 인증 확인

[Url] GET /api/users/verify-email?token={token}<br>
[Desc]: 이메일 인증을 확인하는 엔드포인트<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "이메일 인증이 완료되었습니다!"<br>
}<br>

3. 로그인

[Url] POST /api/users/login<br>
[Desc]:  JWT 토큰 또는 세션을 이용한 로그인 기능<br>
[Request]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"email": "user@example.com",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"password": "password123"<br>
}<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"token": "jwt-token-string",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "로그인 성공!"<br>
}<br>

4. 로그아웃

[Url] POST /api/users/logout<br>
[Desc] 현재 기기에서 로그아웃<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "로그아웃 성공!"<br>
}<br>

5. 모든 기기에서 로그아웃

[Url] POST /api/users/logout/all<br>
[Desc] 모든 기기에서 로그아웃<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "모든 장치에서 로그아웃 되었습니다!"<br>
}<br>

6. 비밀번호 변경

[Url] PUT /api/users/password<br>
[Desc] 비밀번호 변경 시, 모든 기기에서 로그아웃<br>
[Request]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"currentPassword": "oldPassword123",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"newPassword": "newPassword456"<br>
}<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "비밀번호가 변경되었습니다. 모든 장치에서 로그아웃됩니다"<br>
}<br>

<상품 관리>

1. 상품 리스트 조회

[Url] GET /api/products<br>
[Desc] 등록된 상품 목록을 조회<br>
[Request]<br>
<br>
[Response]<br>
[<br>
&nbsp;&nbsp;{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productId": 1,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productName": "상품1",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productPrice": 10000,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"stockQuantity": 20<br>
&nbsp;&nbsp;},<br>
&nbsp;&nbsp;{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productId": 2,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productName": "상품2",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productPrice": 20000,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"stockQuantity": 15<br>
&nbsp;&nbsp;}<br>
]<br>

2. 상품 상세 정보 조회

[Url] GET /api/products/{productId}<br>
[Desc] 특정 상품의 상세 정보를 조회<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productId": 1,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productName": "상품1",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productDescription": "상품 설명",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productPrice": 10000,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"stockQuantity": 20,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"createdAt": "2024-04-01"<br>
}<br>

<주문 관리>

1. 주문하기

[Url] POST /api/orders<br>
[Desc] 상품을 주문<br>
[Request]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productId": 1,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"quantity": 2<br>
}<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"orderId": 123,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "상품 주문 성공"<br>
}<br>

2. 주문 상태 조회

[Url] GET /api/orders<br>
[Desc] 사용자의 주문 상태를 조회<br>
[Request]<br>
<br>
[Response]<br>
[<br>
&nbsp;&nbsp;{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"orderId": 123,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productName": "상품1",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"orderStatus": "배송중",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"orderDate": "2024-04-01"<br>
&nbsp;&nbsp;}<br>
]<br>

3. 주문 취소

[Url] PUT /api/orders/{orderId}/cancel<br>
[Desc] 배송 전 상태인 상품의 주문 취소<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "주문취소 성공!, 재고가 반환됩니다"<br>
}<br>

4. 반품 신청

[Url] PUT /api/orders/{orderId}/return<br>
[Desc] 배송 완료된 상품을 반품 신청<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "반품신청이 완료되었습니다. 회수는 하루가 소요됩니다"<br>
}<br>

<위시리스트 관리>

1. 위시리스트에 상품 추가

[Url] POST /api/wishlist<br>
[Desc] 상품을 위시리스트에 추가합니다.<br>
[Request]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productId": 1,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"quantity": 1<br>
}<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "해당 상품을 '찜' 하였습니다"<br>
}<br>

2. 위시리스트 조회

[Url] GET /api/wishlist<br>
[Desc] 위시리스트에 등록된 상품을 조회<br>
[Request]<br>
<br>
[Response]<br>
[<br>
&nbsp;&nbsp;{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"wishlistId": 1,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productId": 1,<br>
&nbsp;&nbsp;&nbsp;&nbsp;"productName": "상품1",<br>
&nbsp;&nbsp;&nbsp;&nbsp;"quantity": 2<br>
&nbsp;&nbsp;}<br>
]<br>

3. 위시리스트 수정

[Url] PUT /api/wishlist/{wishlistId}<br>
[Desc] 위시리스트 상품 수량을 수정<br>
[Request]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"quantity": 3<br>
}<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "위시리스트 수정이 완료되었습니다"<br>
}<br>

4. 위시리스트 삭제

[Url] DELETE /api/wishlist/{wishlistId}<br>
[Desc] 위시리스트에서 상품을 삭제<br>
[Request]<br>
<br>
[Response]<br>
{<br>
&nbsp;&nbsp;&nbsp;&nbsp;"msg": "상품이 위시리스트에서 삭제되엇씁니다"<br>
}<br>
</details>
<br>
<details>
    <summary>프로젝트 일정 계획표</summary>
    <table>
        <tr>
            <th>/</th>
            <td>목표</td>
            <td>실천</td>
        </tr>
        <tr>
            <th>24.12.18</th>
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
            <th>24.12.19</th>
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
            <th>24.12.20</th>
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
            <th>24.12.21</th>
            <td>
                각 서비스 별 스키마 분할<br>
                API 명세서 보완하여 재작성<br>
                명세서 기반 상품관리 서비스 구현<br>
                ========Optional===========<br>
                주문관리 서비스 구현<br>
                Resilence4j 활용<br>
                장애상황 연출 및 회복탄력성 갖추기
            </td>
            <td>
                서비스 별 스키마 분할 완료<br>
                상품관리 서비스 구현 완료<br>
                주문관리 서비스 구현 중...
            </td>
        </tr>
    </table>
</details>
