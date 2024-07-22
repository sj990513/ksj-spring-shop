package springshopksj.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
   Page<Item> findByCategory(String category, Pageable pageable);

   //Containing이 Like검색 %{keyword}%
   Page<Item> findByItemnameContaining(String keyword, Pageable pageable);

   //카테고리안에서 검색어
   Page<Item> findByCategoryAndItemnameContaining(String category, String keyword, Pageable pageable);

}
