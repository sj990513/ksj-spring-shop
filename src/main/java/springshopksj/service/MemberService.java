package springshopksj.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import springshopksj.dto.CustomUserDetails;
import springshopksj.dto.ItemDto;
import springshopksj.dto.MemberDto;
import springshopksj.dto.ReviewDto;
import springshopksj.entity.Item;
import springshopksj.entity.Member;
import springshopksj.repository.MemberRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    //비밀번호 암호화
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    //이메일
    private final JavaMailSender javaMailSender;

    //인증 문구에 사용될 문자 (영문 소문자 및 숫자)
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";

    //인증용 랜덤문자 만들기
    public String randomAuth() {
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < STRING_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }

    //인증 문구 길이
    private static final int STRING_LENGTH = 6;



    // 중복 확인 메서드
    private Boolean validateDuplicate(Function<MemberDto, String> getField, Function<String, Boolean> existsByField, MemberDto memberDto, String currentValue) {
        String fieldValue = getField.apply(memberDto);
        return !fieldValue.equals(currentValue) && existsByField.apply(fieldValue);
    }

    //회원가입
    public String joinProcess(MemberDto memberDto) {

        Boolean checkUsername = memberRepository.existsByUsername(memberDto.getUsername());
        if (checkUsername)
            return "이미 존재하는 아이디 입니다.";

        Boolean checkNickname = memberRepository.existsByNickname(memberDto.getNickname());
        if (checkNickname)
            return "이미 존재하는 닉네임 입니다.";

        Boolean checkEmail = memberRepository.existsByEmail(memberDto.getEmail());
        if (checkEmail)
            return "이미 존재하는 이메일 입니다.";

        Boolean checkPhone = memberRepository.existsByPhone(memberDto.getPhone());
        if (checkPhone)
            return "이미 존재하는 핸드폰번호 입니다.";

        if (!checkUsername && !checkNickname && !checkEmail && !checkPhone) {
            Member member = modelMapper.map(memberDto, Member.class);
            member.setPassword(bCryptPasswordEncoder.encode(memberDto.getPassword()));
            member.setRole(Member.Role.ROLE_USER);
            member.setProvider(Member.Provider.LOCAL);
            memberRepository.save(member);
        }
        return "회원가입 성공";
    }

    //중복검사들
    public Boolean checkUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    public Boolean checkNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public Boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public Boolean checkPhone(String phone) {
        return memberRepository.existsByPhone(phone);
    }


    //아이디찾기
    public String findId(MemberDto memberDto) {

        Optional<Member> member = memberRepository.findByEmail(memberDto.getEmail());

        if (member.isEmpty())
            return "이메일에 해당하는 아이디가 존재하지 않습니다.";

        //로컬계정아닐시
        else if (member.get().getProvider() != Member.Provider.LOCAL)
            return "소셜 로그인 이메일입니다. 해당 소셜로 로그인해주세요.";

        else
            return member.get().getUsername();
    }

    // 비밀번호찾기 - 이메일 보내기
    public String findPassword(MemberDto memberDto) throws MessagingException {

        Optional<Member> member = memberRepository.findByUsername(memberDto.getUsername());

        // 이메일이 다르면
        if (!memberDto.getEmail().equals(member.get().getEmail()))
            return "입력하신 아이디에 해당하는 계정의 이메일 주소가 다릅니다. 올바르게 입력해주세요.";

        if (member.isEmpty())
            return "아이디에 해당되는 계정이 존재하지 않습니다.";

            //로컬계정아닐시
        else if (member.get().getProvider() != Member.Provider.LOCAL)
            return "소셜 로그인 이메일입니다. 해당 소셜로 로그인해주세요.";


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        String newPassword = randomAuth();

        helper.setFrom("rlatjswo159874@naver.com");
        helper.setTo(memberDto.getEmail());
        helper.setSubject("KSJ-Shop 비밀번호 재설정 안내");

        // HTML email content
        String htmlContent = "<div style='font-family: Arial, sans-serif; font-size: 14px; color: #333;'>"
                + "<h2>안녕하세요 " + member.get().getNickname() + "님,</h2>"
                + "<p>요청하신 비밀번호 재설정을 완료하였습니다.</p>"
                + "<p>아래 <strong>임시 비밀번호</strong>를 사용하여 로그인하시고, 로그인 후 반드시 비밀번호를 변경해 주세요.</p>"
                + "<p style='font-size: 18px; font-weight: bold; color: #d9534f;'>임시 비밀번호: " + newPassword + "</p>"
                + "<p>감사합니다.<br>KSJ-Shop 드림.</p>"
                + "<hr>"
                + "<p style='font-size: 12px; color: #777;'>이 이메일은 발신 전용입니다. 회신하지 마세요.</p>"
                + "</div>";

        helper.setText(htmlContent, true);
        javaMailSender.send(mimeMessage);


        member.get().setPassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member.get());

        return "안내 메일을 전송하였습니다.";
    }

    //모든 member 조회 - 페이징처리
    public Page<MemberDto> findAllMembers(Pageable pageable) {

        Page<Member> allMembers = memberRepository.findAll(pageable);

        return allMembers.map(member -> modelMapper.map(member, MemberDto.class));
    }

    // 모든 member 검색어 포함 조회 - 페이징처리
    public Page<MemberDto> findAllBySearch(String keyword, Pageable pageable) {

        Page<Member> findBySearch = memberRepository.findByUsernameContaining(keyword, pageable);

        return findBySearch.map(member -> modelMapper.map(member, MemberDto.class));
    }

    // 최근 가입한 회원 10명
    public List<MemberDto> recentJoinMember() {

        List<Member> recentMembers = memberRepository.findRecentMembers();

        return recentMembers.stream()
                .map(member -> modelMapper.map(member, MemberDto.class))
                .collect(Collectors.toList());
    }


    // userID로 회원조회
    public MemberDto findById(long userId) {

        Member findMember = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        return modelMapper.map(findMember, MemberDto.class);
    }

    // 권한 업데이트
    public MemberDto updateRole(long userId, MemberDto memberDto) {

        Member findMember = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        findMember.setRole(memberDto.getRole());

        Member updateMember = memberRepository.save(findMember);

        return modelMapper.map(updateMember, MemberDto.class);
    }


    //username으로 회원조회
    public MemberDto fidnByUsername(String username) {

        if(username.equals("anonymousUser"))
            return null;

        Member findMember = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        return  modelMapper.map(findMember, MemberDto.class);
    }

    // 회원 정보 업데이트
    public String updateUserInfo(long userId, MemberDto memberDto) {

        //일단 현재 사용자 정보 불러오고 업데이트할 필드가 현재 필드와 다른 경우에만 중복 검사를 수행
        Member currentMember = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        LocalDateTime createDate = currentMember.getCreateDate();
        Member.Role role = currentMember.getRole();

        Boolean checkNickname = validateDuplicate(MemberDto::getNickname, memberRepository::existsByNickname, memberDto, currentMember.getNickname());
        if (checkNickname)
            return "이미 존재하는 닉네임 입니다.";

        Boolean checkEmail = validateDuplicate(MemberDto::getEmail, memberRepository::existsByEmail, memberDto, currentMember.getEmail());
        if (checkEmail)
            return "이미 존재하는 이메일 입니다.";

        Boolean checkPhone = validateDuplicate(MemberDto::getPhone, memberRepository::existsByPhone, memberDto, currentMember.getPhone());
        if (checkPhone)
            return "이미 존재하는 핸드폰번호 입니다.";

        currentMember = modelMapper.map(memberDto, Member.class);

        //pk값 넣어서 save()메소드가 update로 실행될수있게
        currentMember.setID(userId);
        currentMember.setPassword(bCryptPasswordEncoder.encode(memberDto.getPassword()));
        currentMember.setCreateDate(createDate);
        currentMember.setRole(role);

        memberRepository.save(currentMember);

        return "업데이트 성공";
    }

    //회원 삭제
    public String deleteUser(MemberDto memberDto) {


        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        memberRepository.delete(member);
        return "삭제 성공";
    }

    //회원 삭제 - 관리자
    public String deleteUserByAdmin(MemberDto memberDto, long userId) {

        Member loginUser = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        if (loginUser.getRole().equals(Member.Role.ROLE_ADMIN)) {
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

            memberRepository.delete(member);
            return "삭제 성공";
        }

        else
            return "삭제 실패";
    }
}
