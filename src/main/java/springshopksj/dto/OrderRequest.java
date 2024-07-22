package springshopksj.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private OrderDto orderDto;
    private List<OrderItemDto> orderItems;
    private DeliveryDto deliveryDto;
    private PaymentDto paymentDto;
}
