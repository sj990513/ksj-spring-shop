package springshopksj.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import springshopksj.entity.Member;
import springshopksj.repository.MemberRepository;
import springshopksj.repository.RefreshRepository;
import springshopksj.utils.jwt.JWTUtil;

import java.time.LocalDateTime;

@DataJpaTest
@Slf4j
public class MainControllerTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private  JWTUtil jwtUtil;
    @Autowired
    private RefreshRepository refreshRepository;

    @BeforeEach
    void 기본데이터삽입() {
        Member member1 = Member.builder()
                .password("qwer1234")
                .username(bCryptPasswordEncoder.encode("qwer1234"))
                .email("qwer1234@gmail.com")
                .phone("010-3164-8408")
                .role(Member.Role.ROLE_USER)
                .createDate(LocalDateTime.now())
                .build();

        memberRepository.save(member1);
    }

    @Test
    void 로그인테스트() {

    }





}
