package springshopksj.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springshopksj.dto.DeliveryDto;
import springshopksj.dto.MemberDto;
import springshopksj.dto.OrderItemDto;
import springshopksj.dto.PaymentDto;
import springshopksj.entity.Member;
import springshopksj.entity.Order;
import springshopksj.entity.RefreshToken;
import springshopksj.repository.RefreshRepository;
import springshopksj.service.MemberService;
import springshopksj.service.OrderService;
import springshopksj.service.RefreshTokenService;
import springshopksj.utils.jwt.JWTUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;

    @GetMapping("/")
    public ResponseEntity<?> getUserInfo() {
        //username전달
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        //로그인 유저 존재할시
        if (memberDto != null ) {
            // 여기서 username을 이용하여 추가적인 작업 수행 가능
            log.info("로그인한 유저 이름: {}", memberDto.getUsername());
            
            //로그인성공시 memberDTO객체 반환
            return new ResponseEntity<>(memberDto, HttpStatus.OK);
        }

        // 로그인 유저 업을시
        String msg = "로그인되지 않았습니다.";
        log.info(msg);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }
}
