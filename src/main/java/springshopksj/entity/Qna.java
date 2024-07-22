package springshopksj.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name ="qna")
public class Qna {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;

    @Column(name = "title")
    private String title;

    @Column(name ="content", columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "createdate")
    private LocalDateTime createdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemID", referencedColumnName = "ID")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", referencedColumnName = "ID")
    private Member member;
}
