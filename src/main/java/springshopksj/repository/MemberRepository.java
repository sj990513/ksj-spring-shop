package springshopksj.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Item;
import springshopksj.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> { //extends JpaRepository<엔티티, pk타입>

    List<Member> findFirst2ByUsernameLikeOrderByIDDesc(String username);

    Page<Member> findByUsernameContaining(String keyword, Pageable pageable);

    //username, nickname, email, phone 중복확인
    Boolean existsByUsername(String username);
    Boolean existsByNickname(String nickname);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);
    Optional<Member> findByUsername(String username);

    @Query("SELECT m FROM Member m WHERE m.username = :username")
    Member findByUsn(@Param("username") String username);
    @Query("SELECT m FROM Member m ORDER BY m.createDate DESC")
    List<Member> findRecentMembers();
}
