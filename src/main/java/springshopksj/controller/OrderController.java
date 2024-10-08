package springshopksj.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.*;
import springshopksj.entity.Member;
import springshopksj.entity.Order;
import springshopksj.entity.OrderItem;
import springshopksj.service.MemberService;
import springshopksj.service.OrderService;
import springshopksj.utils.Constants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;

    /**
     * http://localhost:8080/orders
     */
    // 로그인한 사용자 전체 주문내역 페이징 조회 - 사용자
    @GetMapping
    public ResponseEntity<?> getUserOrders(@RequestParam(value = "status", defaultValue = "all") String status,
                                           @RequestParam(value = "page", defaultValue = "1") int page) {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);
        Page<OrderDto> orders;

        if (status.equals("all"))
            orders = orderService.getOrdersByUserId(memberDto, pageable);

        else {
            orders = orderService.findByUserIdAndStatus(memberDto, status, pageable);
        }

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // 로그인한 사용자 특정 주문내역 상세 조회 - 사용자
    /**
     * http://localhost:8080/orders/3
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getUserOrderDetail(@PathVariable(name="orderId") Long orderId) {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderResponse orderResponse = orderService.findOrderDetailByOrderId(memberDto, orderId);

        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    // 주문취소 - 결제전 주문 상태때만 취소가능
    /**
     * http://localhost:8080/orders/3/cancel
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable(name="orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>("주문취소", HttpStatus.OK);
    }


    // 로그인한 사용자 장바구니 조회
    @GetMapping("/cart")
    public ResponseEntity<?> userCart() {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos  = orderService.findAllCart(memberDto);

        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);

    }

    // 장바구니 품목 추가(생성) - 사용자
    /** orderRequest
     * {
     *     "orderItems": [
     *         {
     *             "itemID": 3,
     *             "count": 3
     *         }
     *     ]
     * }
     */
    @PostMapping("/cart/add-cart")
    public ResponseEntity<?> createCart(@RequestBody OrderRequest orderRequest) {
        
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos = orderService.addCart(memberDto, orderRequest.getOrderItems());

        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);
    }

    // 장바구니 품목 수량 수정
    /** orderRequest
     * {
     *     "orderItems": [
     *         {
     *             "id" :61,
     *             "itemID": 3,
     *             "count": 3
     *         }
     *     ]
     * }
     */
    @PatchMapping("/cart/update")
    public ResponseEntity<?> updateCart(@RequestBody OrderRequest orderRequest) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos = orderService.updateCartItems(memberDto, orderRequest.getOrderItems());

        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);
    }



    // 장바구니 품목 삭제 - 사용자
    /** 
     * http://localhost:8080/orders/cart/3/delete-item
     */
    @DeleteMapping("/cart/{order_itemID}/delete-item")
    public ResponseEntity<?> deleteCartItem(@PathVariable(name="order_itemID") long order_itemID) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<OrderItemDto> orderItemDtos  = orderService.deleteCartItem(memberDto, order_itemID);
        
        return new ResponseEntity<>(orderItemDtos, HttpStatus.OK);
    }

    //장바구니 전체 삭제
    @DeleteMapping("/cart/delete-cart")
    public ResponseEntity<?> deleteCart() {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        orderService.deleteCart(memberDto);

        return new ResponseEntity<>("장바구니 삭제성공", HttpStatus.OK);
    }

    // 주문 - 사용자
    /**
     * orderRequest
     * {
     *     "deliveryDto": {
     *         "address": "example korea 1234",
     *         "orderID": 93
     *     }
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<?> orderFromCart(@RequestBody OrderRequest orderRequest) {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderDto orderDto  = orderService.createOrderDetatil(memberDto, orderRequest.getDeliveryDto());

        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }


    // 아이템 디테일에서 즉시 주문 - 사용자
    /**
     * orderRequest
     * {
     *     "orderItems": [
     *         {
     *             "itemID": 3,
     *             "count": 3
     *         }
     * }
     */
    @PostMapping("/order-now")
    public ResponseEntity<?> orderNow(@RequestBody OrderRequest orderRequest) {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderDto orderDto = orderService.createOrder(memberDto, orderRequest.getOrderItems());

        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }
    //주문 검증 및 예외처리
}
