package springshopksj.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Qna;
import springshopksj.entity.Review;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long> {
    Page<Qna> findByItemID(long itemId, Pageable pageable);

}
