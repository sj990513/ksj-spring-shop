package springshopksj.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

   // 모든 아이템들은 최근 만들어진 순서대로
   @Query("SELECT i FROM Item i ORDER BY i.createDate DESC")
   Page<Item> findAllItems(Pageable pageable);

   //Containing이 Like검색 %{keyword}%
   @Query("SELECT i FROM Item i WHERE i.itemname LIKE %:keyword% ORDER BY i.createDate DESC")
   Page<Item> findByItemnameContaining(@Param("keyword") String keyword, Pageable pageable);

   @Query("SELECT i FROM Item i WHERE i.category = :category ORDER BY i.createDate DESC")
   Page<Item> findByCategory(@Param("category") String category, Pageable pageable);

   //카테고리안에서 검색어
   @Query("SELECT i FROM Item i WHERE i.category = :category AND i.itemname LIKE %:keyword% ORDER BY i.createDate DESC")
   Page<Item> findByCategoryAndItemnameContaining(@Param("category") String category, @Param("keyword") String keyword, Pageable pageable);

}
