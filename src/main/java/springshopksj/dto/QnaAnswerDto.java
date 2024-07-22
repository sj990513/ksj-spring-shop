package springshopksj.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QnaAnswerDto {
    private long ID;
    private String answer;
    private LocalDateTime createdate;
    private long qnaID;
}
