package springshopksj.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDetailDto {

    ItemDto itemDto;
    List<ReviewDto> reviewListDto;

}
