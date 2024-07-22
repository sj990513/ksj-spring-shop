package springshopksj.dto;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import springshopksj.entity.Item;
import springshopksj.entity.Order;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private long ID;
    private int orderprice;
    private int count;
    private long orderID;
    private long itemID;
}