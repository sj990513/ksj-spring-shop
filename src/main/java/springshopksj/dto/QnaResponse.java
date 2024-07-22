package springshopksj.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QnaResponse {
    private QnaDto qnaDto;
    private QnaAnswerDto qnaAnswerDto;
}
