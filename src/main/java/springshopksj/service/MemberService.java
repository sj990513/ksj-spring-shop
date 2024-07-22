package springshopksj.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import springshopksj.dto.CustomUserDetails;
import springshopksj.dto.ItemDto;
import springshopksj.dto.MemberDto;
import springshopksj.dto.ReviewDto;
import springshopksj.entity.Item;
import springshopksj.entity.Member;
import springshopksj.repository.MemberRepository;

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
}
