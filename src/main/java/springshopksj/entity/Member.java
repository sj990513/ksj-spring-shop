package springshopksj.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name ="member")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)   //KEY값과 자동증가되는값
    private long ID;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @CreatedDate
    @Column(name = "createdate")
    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "varchar(255)")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", columnDefinition = "varchar(255)")
    private Provider provider;

    public enum Role {
        ROLE_ADMIN, ROLE_USER
    }

    public enum Provider {
        LOCAL, NAVER, GOOGLE
    }

}
