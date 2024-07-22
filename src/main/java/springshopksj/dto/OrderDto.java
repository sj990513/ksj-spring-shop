package springshopksj.dto;

import lombok.*;
import springshopksj.entity.Order;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private long id;
    private Order.OrderStatus status;
    private LocalDateTime orderDate;
    private long totalprice;
    private long userID;
}
