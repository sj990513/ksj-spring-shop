package springshopksj.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import springshopksj.entity.Member;

import java.util.Map;

@Getter
@Setter
@Builder
public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {

        this.attribute = attribute;
    }

    @Override
    public Member.Provider getProvider() {

        return Member.Provider.GOOGLE;
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }

    @Override
    public String getName() {

        return attribute.get("name").toString();
    }
}