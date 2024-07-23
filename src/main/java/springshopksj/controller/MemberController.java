package springshopksj.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.MemberDto;
import springshopksj.entity.Member;
import springshopksj.service.MemberService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    //회원가입
    /**
     * memberDto
     *{
     *     "username": "example",
     *     "password": "example",
     *     "nickname": "example",
     *     "email": "example@example.com",
     *     "phone": "01012345678",
     *     "address": "example"
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<String> createUser(@RequestBody MemberDto memberDto) {
        String message = memberService.joinProcess(memberDto);

        switch (message) {
            case "회원가입 성공":
                return new ResponseEntity<>(message, HttpStatus.OK);
            case "이미 존재하는 아이디 입니다.":
            case "이미 존재하는 닉네임 입니다.":
            case "이미 존재하는 이메일 입니다.":
            case "이미 존재하는 핸드폰번호 입니다.":
                return new ResponseEntity<>(message, HttpStatus.CONFLICT);
            default:
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
    }

    //아이디 중복검사
    @GetMapping("/signup/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam(value="username") String username) {
        return new ResponseEntity<>(memberService.checkUsername(username), HttpStatus.OK);
    }

    //닉네임 중복검사
    @GetMapping("/signup/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam(value="nickname") String nickname) {
        return new ResponseEntity<>(memberService.checkNickname(nickname), HttpStatus.OK);
    }

    //이메일 중복검사
    @GetMapping("/signup/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam(value="email") String email) {
        return new ResponseEntity<>(memberService.checkEmail(email), HttpStatus.OK);
    }

    //핸드폰 중복검사
    @GetMapping("/signup/check-phone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam(value="phone") String phone) {
        return new ResponseEntity<>(memberService.checkPhone(phone), HttpStatus.OK);
    }

    //마이페이지
    @GetMapping("/user-info/user")
    public ResponseEntity<?> myPage() {
        
        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        return new ResponseEntity<>(memberDto, HttpStatus.OK);
    }

    //마이페이지 수정
    /**
     * memberDto
     *{
     *     "username": "example",
     *     "password": "example",
     *     "nickname": "example",
     *     "email": "example@example.com",
     *     "phone": "01012345678",
     *     "address": "example"
     * }
     */
    @PatchMapping("/user-info/user/update")
    public ResponseEntity<?> updateInfo(@RequestBody MemberDto memberDto) {

        //현재 로그인중인 사용자
        MemberDto loginUser = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        
        //requestMemberDto값은 바꾸든 바꾸지않든 기존의 모든값들이 전부 담기게 프론트에서 처리해주어야한다.
        // (username, password, nickname, email, phone, address값들)
        String message = memberService.updateUserInfo(loginUser.getID(), memberDto);

        if (message.equals("업데이트 성공")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
        }
    }

    //회원삭제
    @DeleteMapping("/user-info/user/delete-user")
    public ResponseEntity<?> deleteUser() {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());


        return new ResponseEntity<>(memberService.deleteUser(memberDto), HttpStatus.OK);
    }
}
