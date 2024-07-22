package springshopksj.dto;

import lombok.*;
import springshopksj.entity.Delivery;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDto {
    private String address;
    private Delivery.DeliveryStatus status;
    private long orderID;

}