package springshopksj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import springshopksj.dto.*;
import springshopksj.entity.Member;
import springshopksj.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();

        Member existData = memberRepository.findByUsn(username);

        if (existData == null) {

            String nickname = oAuth2Response.getName();
            String uniqueNickname = nickname;
            int suffix = 1;

            // 닉네임이 중복될 경우 중복되지 않는 닉네임을 찾을 때까지 반복
            while ( memberRepository.existsByNickname(uniqueNickname) ) {
                uniqueNickname = nickname + suffix;
                suffix++;
            }

            Member member = Member.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .nickname(uniqueNickname) // 중복되지 않는 닉네임 설정
                    .provider(oAuth2Response.getProvider())
                    .role(Member.Role.ROLE_USER)
                    .build();

            memberRepository.save(member);

            MemberDto memberDto = MemberDto.builder()
                    .username(username)
                    .nickname(uniqueNickname)
                    .provider(oAuth2Response.getProvider())
                    .role(Member.Role.ROLE_USER)
                    .build();

            return new CustomOAuth2User(memberDto);
        }

        else {

            MemberDto memberDto = MemberDto.builder()
                    .username(existData.getUsername())
                    .nickname(existData.getNickname())
                    .role(existData.getRole())
                    .provider(existData.getProvider())
                    .build();

            return new CustomOAuth2User(memberDto);
        }
    }
}