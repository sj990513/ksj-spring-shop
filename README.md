# 🛒 spring-shop-ksj
> 상품들을 조회, 구매 가능한 쇼핑몰 프로젝트입니다.
<br>
<br>

## 🗓️ 기간 / 인원
- **기간:** 24.06.13 ~ 24.08.22
- **인원:** 개인 개발 프로젝트
<br>

## 🔑 핵심 기능
- OAuth2 JWT 소셜 로그인 기능
- 핸드폰, 이메일 인증
- 카카오페이 주문
- 상품 주문, 배달현황 조회
<br>

## 📋 세부내용
- JWT 다중 토큰(access, refresh) 발급 방식 구현
- Spring JPA 활용
- REST API 개발
- 소셜 로그인 & 카카오페이 기능 구현
- Spring과 React를 이용한 백-프론트 연동
- 각종 검색 & 열람 기능 구현
- 상품, 리뷰, QnA & QnA 답변 구현
<br>

## 💻 개발 환경

### Backend
- **언어 / 프레임워크:**  
  Java 21, Spring Boot 3.2.5
- **Security:**  
  Spring Security, JWT
- **DB:**  
  Spring Data JPA

### Frontend
- **언어 / 프레임워크:**  
  React, HTML/CSS
- **API:**  
  Axios

### DB
- **MySQL**

### Server
- **Hosting:**  
  AWS EC2 (Linux OS)
- **Web Server:**  
  NGINX

### Utilities
- **인증:**  
  JWT, OAuth2 Client
- **검증:**  
  Mail Starter, Nurigo SMS
- **보안:**  
  Https 적용

### Tools
- **IDE / Editors:**  
  IntelliJ IDEA, VS Code, MySQL Workbench, Postman
- **Version Control & CI/CD:**  
  GitHub
<br>

## 🛠️ 시스템 구성도
![SPRING SHOP drawio](https://github.com/user-attachments/assets/3c73ac2f-9cbe-4d51-92cc-9eb0c7fb1363)
<br>

## 🗂️ 데이터베이스 구조 (ERD)
![erd](https://github.com/user-attachments/assets/4bb689ad-dd85-4f84-99ee-5b8dfae43904)
<br>

## 구현 - 사용자
<br>

### 1. 이메일 인증 & 핸드폰 인증
![ksjshoperd drawio (1)](https://github.com/user-attachments/assets/61536be4-6a43-4760-8512-726d69bda796)
<br>
<br>


### 2. 소셜 로그인 & 카카오 페이 결제
![ksjshoperd drawio](https://github.com/user-attachments/assets/23fb2c16-071c-4a7a-90d9-7d7066ba839b)
<br>
<br>


### 3. 상품 정렬 (페이징처리)
![상품리스트 drawio](https://github.com/user-attachments/assets/51677865-297c-4821-99bd-a0c1a02ba3a0)
<br>
<br>


### 4. 상품 상세보기, 리뷰, QnA
![tv상세보기 drawio](https://github.com/user-attachments/assets/14b1d46d-b28d-4d0c-9a52-632b32177c58)
<br>
<br>


### 5. 마이페이지 (개인정보수정, 주면현황 & 주문상세 조회, 장바구니 조회 & 결제)
![마이페이지 drawio](https://github.com/user-attachments/assets/8f71e7d0-c2b8-4409-8fe2-5d4e7675a088)
<br>
<br>


## 구현 - 관리자
<br>

### 1. 대시보드, 주문관리 (주문상태변경), QnA 관리 (답변달기), 회원 관리 (회원 권한변경 & 삭제)
![ㄱㄹㅈ1 drawio](https://github.com/user-attachments/assets/186f057d-e74e-436f-b97c-4e2d926bd33d)
<br>
<br>

### 2. 상품 관리 (상품 조회, 수정, 삭제, 추가)
![ㄱㄹㅈ2 drawio](https://github.com/user-attachments/assets/27c46457-4407-424f-a1d6-5f01931175d1)
<br>
<br>
