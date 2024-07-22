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

            Member member = Member.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .nickname(oAuth2Response.getName())
                    .provider(oAuth2Response.getProvider())
                    .role(Member.Role.ROLE_USER)
                    .build();

            memberRepository.save(member);

            MemberDto memberDto = MemberDto.builder()
                    .username(username)
                    .nickname(oAuth2Response.getName())
                    .provider(oAuth2Response.getProvider())
                    .role(Member.Role.ROLE_USER)
                    .build();

            return new CustomOAuth2User(memberDto);
        }

        else {

            existData.setEmail(oAuth2Response.getEmail());
            existData.setNickname(oAuth2Response.getName());

            memberRepository.save(existData);

            MemberDto memberDto = MemberDto.builder()
                    .username(existData.getUsername())
                    .nickname(oAuth2Response.getName())
                    .role(existData.getRole())
                    .provider(oAuth2Response.getProvider())
                    .build();

            return new CustomOAuth2User(memberDto);
        }
    }
}