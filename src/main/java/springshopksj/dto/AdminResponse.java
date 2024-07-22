package springshopksj.dto;

import lombok.*;
import springshopksj.entity.Order;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponse {
    List<MemberDto> memberDtos;
    List<OrderDto> orderDtos;
}
