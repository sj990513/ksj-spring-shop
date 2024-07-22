package springshopksj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import springshopksj.entity.RefreshToken;
import springshopksj.repository.RefreshRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshRepository refreshRepository;

    //refresh toeken저장
    public RefreshToken saveToken(RefreshToken refreshToken) {
        return refreshRepository.save(refreshToken);
    }

    //refresh 토큰 조회
    public RefreshToken findByRefreshToken(String refresh) {
        return refreshRepository.findByRefresh(refresh);
    }

    //토큰 검증
    public Boolean existsByRefreshToken(String refresh) {
        return refreshRepository.existsByRefresh(refresh);
    }

    //토큰삭제
    public void deleteByRefreshToken(String refresh) {
        refreshRepository.deleteByRefresh(refresh);
    }


    //refresh token안의 사용자 정보 가져오기
    public RefreshToken findByRefReshTokenValue(String refreshTokenValue) {
        return refreshRepository.findByRefresh(refreshTokenValue);
    }

    //매일 새벽2시 실행, 만료시간 지난 refresh토큰들 db에서 삭제
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshRepository.deleteByExpirationBefore(now);
    }
}