package springshopksj.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final MemberDto memberDto;

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return String.valueOf(memberDto.getRole());
            }
        });

        return collection;
    }

    @Override
    public String getName() {

        return memberDto.getNickname();
    }

    public String getUsername() {

        return memberDto.getUsername();
    }
}