package springshopksj.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springshopksj.entity.RefreshToken;
import springshopksj.repository.RefreshRepository;
import springshopksj.service.RefreshTokenService;
import springshopksj.utils.jwt.JWTUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;


//refresh토큰 재전송 컨트롤러
@Controller
@ResponseBody
@RequiredArgsConstructor
@Slf4j
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;


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

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        LocalDateTime date =  LocalDateTime.now().plus(Duration.ofMillis(expiredMs));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setRefresh(refresh);
        refreshToken.setExpiration(date);

        refreshTokenService.saveToken(refreshToken);
    }
}