package springshopksj.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springshopksj.entity.Order;
import springshopksj.entity.OrderItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByMemberIDAndStatus(long userID, Order.OrderStatus status);

    // orderID내림차순
    @Query("SELECT o FROM Order o WHERE o.member.ID = :userID ORDER BY o.ID DESC")
    Page<Order> findByMemberID(@Param("userID") long userID, Pageable pageable);

    // orderID내림차순
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.ID DESC")
    Page<Order> findByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);

    // orderID 내림차순으로 정렬된 특정 사용자와 상태의 주문 목록을 조회
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.member.ID = :userID ORDER BY o.ID DESC")
    Page<Order> findByStatusAndMemberId(@Param("userID") long userID, @Param("status") Order.OrderStatus status, Pageable pageable);

    // 페이징x
    List<Order> findByStatus(Order.OrderStatus status);

    // 배송완료된 상품만 리뷰작성가능, jpql사용하니 별칭사용
    @Query("SELECT oi FROM OrderItem oi WHERE oi.item.ID = :itemID AND oi.order.ID IN " +
            "(SELECT o.ID FROM Order o WHERE o.member.ID = :memberID AND o.status = 'DELIVERED')")
    List<OrderItem> findDeliveredOrderItemsByMemberAndItem(@Param("memberID") long memberID, @Param("itemID") long itemID);
}
