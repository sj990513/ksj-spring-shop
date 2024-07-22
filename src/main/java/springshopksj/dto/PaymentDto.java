package springshopksj.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private int amount;
    private String paymentMethod;
    private LocalDateTime paymentdate;
    private long orderID;
    private String itemName;
}