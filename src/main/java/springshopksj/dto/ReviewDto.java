package springshopksj.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private long ID;
    private int rating;
    private String comment;
    private LocalDateTime createDate;
    private long itemID;
    private long userID;
}
