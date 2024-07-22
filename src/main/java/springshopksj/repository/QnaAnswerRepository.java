package springshopksj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springshopksj.entity.QnaAnswer;

import java.util.Optional;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
    QnaAnswer findByQnaID(Long qnaId);
    Optional<QnaAnswer> findOptionalByQnaID(Long qnaId);
}
