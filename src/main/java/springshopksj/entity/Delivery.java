package springshopksj.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;

    private String address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderID")
    private Order order;
    // 기타 배송 정보 필드
    public enum DeliveryStatus {
        READY,       // 배송 준비 중
        SHIPPED,     // 배송 중
        DELIVERED,   // 배송 완료
        CANCELLED    // 배송 취소
    }
}