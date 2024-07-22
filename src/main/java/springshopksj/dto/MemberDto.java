package springshopksj.dto;
import lombok.*;
import springshopksj.entity.Member;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {

    private long ID;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String address;

    private LocalDateTime createDate;

    private Member.Role role;

    private Member.Provider provider;
}
