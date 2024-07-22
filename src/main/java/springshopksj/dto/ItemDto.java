package springshopksj.dto;

import lombok.*;
import springshopksj.entity.Member;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private long id;

    private String itemname;

    private int price;

    private int stock;

    private String category;

    private String description;

    private String imageUrl;

    private LocalDateTime createDate;

    private long userID;

}