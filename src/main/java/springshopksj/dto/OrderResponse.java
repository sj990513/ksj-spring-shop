package springshopksj.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private OrderDto orderDto;
    private List<OrderItemDto> orderItemDtos;
    private DeliveryDto deliveryDto;
    private PaymentDto paymentDto;
}
