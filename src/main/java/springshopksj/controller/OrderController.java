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
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;


    // 로그인한 사용자 전체 주문내역 페이징 조회 - 사용자
    @GetMapping
    public ResponseEntity<?> getUserOrders(@RequestParam(value = "page", defaultValue = "1") int page) {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<OrderDto> orders = orderService.getOrdersByUserId(memberDto, pageable);

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

    // 주문취소 - 사용자
    /**
     * http://localhost:8080/orders/3/cancle
     */
    @PatchMapping("/{orderId}/cancle")
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

        List<OrderItemDto> orderItemDtos  = orderService.addCart(memberDto, orderRequest.getOrderItems());

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

    // 장바구니에서 주문 - 사용자
    /**
     * orderRequest
     * {
     *     "deliveryDto": {
     *         "address": "example korea 1234"
     *     }
     * }
     */
    @PostMapping("/cart/create")
    public ResponseEntity<?> orderFromCart(@RequestBody OrderRequest orderRequest) {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderDto orderDto  = orderService.createOrderFromCart(memberDto, orderRequest.getDeliveryDto());

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
     *     ],
     *     "deliveryDto": {
     *         "address": "123 Main St, Anytown, USA"
     *     }
     * }
     */
    @PostMapping("/order-now")
    public ResponseEntity<?> orderNow(@RequestBody OrderRequest orderRequest) {

        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        OrderDto orderDto = orderService.createOrder(memberDto, orderRequest.getOrderItems(), orderRequest.getDeliveryDto());

        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }
    //주문 검증 및 예외처리
}
