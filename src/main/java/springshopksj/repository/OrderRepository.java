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
    Page<Order> findByMemberID(long userID, Pageable pageable);

    Optional<Order> findByMemberIDAndStatus(long userID, Order.OrderStatus status);

    // 페이징
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // 페이징x
    List<Order> findByStatus(Order.OrderStatus status);

    // 배송완료된 상품만 리뷰작성가능, jpql사용하니 별칭사용
    @Query("SELECT oi FROM OrderItem oi WHERE oi.item.ID = :itemID AND oi.order.ID IN " +
            "(SELECT o.ID FROM Order o WHERE o.member.ID = :memberID AND o.status = 'DELIVERED')")
    List<OrderItem> findDeliveredOrderItemsByMemberAndItem(@Param("memberID") long memberID, @Param("itemID") long itemID);
}
