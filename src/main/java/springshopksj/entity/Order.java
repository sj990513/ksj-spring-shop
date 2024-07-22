package springshopksj.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @CreatedDate
    private LocalDateTime orderdate;

    private long totalprice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", referencedColumnName = "ID")
    private Member member;


    // 기타 주문 정보 필드
    public enum OrderStatus {

        PENDING,     // 뵤류중 (장바구니)
        ORDERED,     // 주문 완료
        PAID,        // 결제 완료
        CANCEL,      // 주문 취소 요청
        CANCELLED,   // 주문 취소
        SHIPPED,     // 배송 중
        DELIVERED    // 배송 완료
    }
}