package springshopksj.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.RefreshToken;

import java.time.LocalDateTime;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByRefresh(String refresh);
    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

    //만료시간 지난 토큰 삭제
    @Transactional
    void deleteByExpirationBefore(LocalDateTime now);
}