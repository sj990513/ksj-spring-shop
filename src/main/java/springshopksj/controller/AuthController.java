package springshopksj.controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.MemberDto;
import springshopksj.entity.Authnumber;
import springshopksj.service.AuthService;
import springshopksj.service.MemberService;

import java.util.Map;
import java.util.Random;

@RestController
public class AuthController {

    private final DefaultMessageService messageService;
    private final AuthService authService;
    private final MemberService memberService;

    public AuthController(AuthService redisService, @Value("${sms.api}")String api, @Value("${sms.secret}")String secret, MemberService memberService) {
        // 반드시 계정 내 등록된 유효한 API 키, API Secret Key를 입력해주셔야 합니다!

        this.messageService = NurigoApp.INSTANCE.initialize(api, secret, "https://api.coolsms.co.kr");
        this.authService = redisService;
        this.memberService = memberService;
    }

    // 로그인 상태 체크
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(@AuthenticationPrincipal UserDetails userDetails) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        
        if (memberDto != null) {
            return new ResponseEntity<>(memberDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("비로그인 상태", HttpStatus.UNAUTHORIZED);
        }
    }

    // 문자인증
    @PostMapping("/signup/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> request) {

        String phoneNumber = request.get("phoneNumber");
        //랜덤 인증번호 생성
        String verificationCode = generateVerificationCode();

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        // from: 발신번호
        // to: 수신번호
        // text: 보낼 문자 내용
        message.setFrom("01031648408");
        message.setTo(phoneNumber);
        message.setText("[spring-shop-ksj발송]\n인증번호 : " + verificationCode);

        //저장전에 이미 핸드폰번호로 인증번호 존재할시 삭제
        Authnumber authNumber = authService.findByPhoneNumber(phoneNumber);
        if (authNumber != null)
            authService.deleteAuthNumber(phoneNumber);

        //폰 번호와 인증번호 저장
        authService.saveAuthNumber(phoneNumber, verificationCode);

        //진짜 문자보낼땐 주석제거
        //SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));

        return new ResponseEntity<>("인증번호 전송!", HttpStatus.OK);
    }


    //문자인증확인
    @PostMapping("/signup/auth")
    public ResponseEntity<?> authCode(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String code = request.get("code");

        // 인증번호 확인
        Authnumber authNumber = authService.findByPhoneNumber(phoneNumber);

        if (authNumber != null && authNumber.getCode().equals(code)) {
            authService.deleteAuthNumber(phoneNumber);
            return new ResponseEntity<>("핸드폰 인증 성공", HttpStatus.OK);
        } else {
            authService.deleteAuthNumber(phoneNumber);
            return new ResponseEntity("인증번호가 일치하지않습니다. 재인증 부탁드립니다.", HttpStatus.UNAUTHORIZED);
        }
    }

    //랜덤 인증번호 생성
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}