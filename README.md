# 🛒 spring-shop-ksj
> 상품들을 조회, 구매 가능한 쇼핑몰 프로젝트입니다.

https://ksjshop.shop

관리자 계정 체험 - 아이디 : qwer1234 / 비밀번호 : qwer1234
<br>
<br>

## 🗓️ 기간 / 인원
- **기간:** 24.05.13 ~ 24.08.22
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

## 🔧 주요 이슈
 ### oauth2 소셜 로그인
 oauth2 소셜 로그인 진행시 header에 jwt access token을 직접 지급하지 못하는 현상이 발생  
 front에서 refresh token만 cookie에 저장 후, cookie에 담긴 refresh token값을 이용해 서버에 재발급 요청(reissue)을 하여 해결
 
 <b> 1. oauth2 로그인시 서버에서 refresh token만을 cookie에 저장</b>
    
```java
    // CustomSuccessHandler.java 일부
    @Override
      public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
  
          //OAuth2User
          CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
  
          String username = customUserDetails.getUsername();
  
          Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
          Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
          GrantedAuthority auth = iterator.next();
          String role = auth.getAuthority();
  
  
          String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
  
          response.addCookie(createCookie("refresh", refresh));
          response.sendRedirect(frontUrl);
  
          //Refresh 토큰 저장
          addRefreshEntity(username, refresh, 86400000L);
      }
  
      private Cookie createCookie(String key, String value) {
  
          Cookie cookie = new Cookie(key, value);
          cookie.setMaxAge(60*60*60);
          cookie.setSecure(true);
          cookie.setPath("/");
          cookie.setHttpOnly(true);
  
          return cookie;
      }
```


<b> 2. front에서 서버로 reissue 요청 - header에 access token이 존재하지 않기때문에 자동으로 reissue 요청 진행.</b>
    
```javascript
    // axiosInstance.js 일부
    axiosInstance.interceptors.response.use(
      response => response,
      async error => {
        const originalRequest = error.config;
    
        // 에러 응답 상태가 401이고 재시도가 아니며 'access token expired'인 경우
        if (error.response.status === 401 && !originalRequest._retry && error.response.data === 'access token expired') {
          originalRequest._retry = true;
    
          try {
            // 토큰 갱신 시도
            const response = await axiosInstance.post('/reissue');
            const newAccessToken = response.headers['access'];
            localStorage.setItem('access', newAccessToken);
            axiosInstance.defaults.headers.common['access'] = newAccessToken;
    
            // 새로운 토큰으로 원래 요청을 재시도
            originalRequest.headers['access'] = newAccessToken;
            return axiosInstance(originalRequest);
          } catch (reissueError) {
            console.error('토큰 갱신 실패', reissueError);
    
            // 갱신 토큰이 만료된 경우 액세스 토큰 제거 및 메인 페이지로 리디렉션
            localStorage.removeItem('access');
            window.location.href = '/';
          }
        }
    
        return Promise.reject(error);
      }
    );
```

  <b> 3. /api/reissue로 요청이 들어오면 header에 access token 재발행 </b>
    
```java
    // ReissueController.java 일부
    @PostMapping("/api/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // refresh token 받아오기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();

            }
        }


        if (refresh == null) {

            //response status code
            return new ResponseEntity<>("refresh token이 null값입니다.", HttpStatus.BAD_REQUEST);
        }

        // 만료체크
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token 만료", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("refresh token형식이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshTokenService.existsByRefreshToken(refresh);

        if (!isExist) {

            //response body
            return new ResponseEntity<>("refresh token이 db에 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

       log.info("access token 재발행 성공");

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshTokenService.deleteByRefreshToken(refresh);
        addRefreshEntity(username, newRefresh, 86400000L);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>("access토큰 재발행 성공", HttpStatus.OK);
    }
```

<br>
<br>

## 구현 - 사용자
<br>

### 1. 이메일 & 핸드폰 검증
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
