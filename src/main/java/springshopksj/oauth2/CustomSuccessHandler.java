package springshopksj.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import springshopksj.dto.CustomOAuth2User;
import springshopksj.entity.RefreshToken;
import springshopksj.repository.RefreshRepository;
import springshopksj.utils.jwt.JWTUtil;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

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

        /**
         프론트와 백이 분리된 시스템에서 OAuth2 소셜 로그인을 진행하면 헤더 방식으로 JWT 자체를 발급받는 게 불가능
         이로인해 프론트로 refresh token만 쿠키로 전송하고, 프론트에서 다시 refresh토큰을 이용해 access토큰을 재발급 받으면된다.
         */
        response.addCookie(createCookie("refresh", refresh));
        response.sendRedirect("http://localhost:3000/");

        //Refresh 토큰 저장
        addRefreshEntity(username, refresh, 86400000L);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        //cookie.setSecure(true);
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

        refreshRepository.save(refreshToken);
    }
}