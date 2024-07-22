package springshopksj.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QnaDto {
    private long ID;
    private String title;
    private String content;
    private LocalDateTime createdate;
    private long itemID;
    private long userID;
}