package springshopksj.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.*;
import springshopksj.entity.Order;
import springshopksj.entity.QnaAnswer;
import springshopksj.service.DeliveryService;
import springshopksj.service.ItemService;
import springshopksj.service.MemberService;
import springshopksj.service.OrderService;
import springshopksj.utils.Constants;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final ItemService itemService;
    private final DeliveryService deliveryService;

    //카카오페이 결제 만들기

    
    
    //관리자 페이지 메인 - 최근 가입한 회원 10명, 입금후 배송대기중인 주문들 리스트
    @GetMapping
    public ResponseEntity<?> amdinMain() {

        // 최근 가입한 회원 10명
        List<MemberDto> recentJoinMember = memberService.recentJoinMember();

        // 배송 대기중인 주문 리스트 (PAID 상태)
        List<OrderDto> paidOrderList = orderService.findByStatusList(Order.OrderStatus.PAID);

        AdminResponse adminResponse = AdminResponse.builder()
                .memberDtos(recentJoinMember)
                .orderDtos(paidOrderList)
                .build();

        return new ResponseEntity<>(adminResponse, HttpStatus.OK);
    }


    //모든 멤버조회
    /**
     * http://localhost:8080/admin/member-list
     */
    @GetMapping("/member-list")
    public ResponseEntity<?> allMemberList(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "search", required = false) String search) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<MemberDto> findBySearch = memberService.findAllBySearch(search, pageable);
            return new ResponseEntity<>(findBySearch, HttpStatus.OK);
        }

        Page<MemberDto> allMembers = memberService.findAllMembers(pageable);

        return new ResponseEntity<>(allMembers, HttpStatus.OK);
    }

    // 회원 권한 변경 - ROLE_USER, ROLE_ADMIN
    /**
     * memberDto
     *{
     *     "role": "ROLE_ADMIN"
     * }
     */
    @PatchMapping("/member-list/{userId}/update-role")
    public ResponseEntity<?> updateMemberRole(@PathVariable(name = "userId") long userId, @RequestBody MemberDto memberDto) {

        MemberDto updateMemberDto = memberService.updateRole(userId, memberDto);

        return new ResponseEntity<>(updateMemberDto, HttpStatus.OK);
    }

    //회원삭제
    @DeleteMapping("/member-list/{userId}/delete")
    public ResponseEntity<?> deleteMember(@PathVariable(name = "userId") long userId) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String msg = memberService.deleteUserByAdmin(memberDto, userId);

        if (msg.equals("삭제 성공"))
            return new ResponseEntity<>(msg, HttpStatus.OK);

        else
            return new ResponseEntity<>(msg, HttpStatus.UNAUTHORIZED);

    }


    // 전체 아이템조회
    /**
     * http://localhost:8080/admin/item-list?page=3&search=example
     */
    @GetMapping("/item-list")
    public ResponseEntity<?> allItems(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "search", required = false) String search) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<ItemDto> findBySearch = itemService.findBySearch(search, pageable);
            return new ResponseEntity<>(findBySearch, HttpStatus.OK);
        }

        Page<ItemDto> allItemList = itemService.findAllItems(pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(allItemList, HttpStatus.OK);
    }

    // 아이템 추가 - 관리자만 가능
    /** http://localhost:3000/admin/item-list/add-item
     * itemDto
     * {
     *     "itemname": "Example 관리자",
     *     "price": 100,
     *     "stock": 50,
     *     "category": "top",
     *     "description": "This is an example item.",
     *     "imageUrl": "http://example.com/image.jpg"
     * }
     */
    @PostMapping("/item-list/add-item")
    public ResponseEntity<?> addItem(@RequestBody ItemDto itemDto) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String message = itemService.addItem(itemDto, memberDto);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // 아이템 수정 - 관리자만 가능
    /**
     * http://localhost:8080/admin/item-list/3/update
     *
     * itemDto
     * {
     *     "itemname": "Example 관리자",
     *     "price": 100,
     *     "stock": 50,
     *     "category": "top",
     *     "description": "This is an example item.",
     *     "imageUrl": "http://example.com/image.jpg"
     * }
     */
    @PatchMapping("/item-list/{itemId}/update")
    public ResponseEntity<?> updateItem(@PathVariable(name = "itemId") long itemId,
                                        @RequestBody ItemDto itemDto) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        ItemDto updateItem = itemService.updateItem(itemId, itemDto, memberDto);

        return new ResponseEntity<>(updateItem, HttpStatus.OK);
    }

    // 아이템삭제
    /**
     * http://localhost:8080/admin/item-list/3/delete-item
     */
    @DeleteMapping("/item-list/{itemId}/delete-item")
    public ResponseEntity<?> deleteItem(@PathVariable(name = "itemId") long itemId) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String message = itemService.deleteItem(itemId, memberDto);

        if(message.equals("삭제성공"))
            return new ResponseEntity<>(message, HttpStatus.OK);

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }


    // 모든 qna 조회
    /**
     * http://localhost:8080/admin/qna
     */
    @GetMapping("/qna")
    public ResponseEntity<?> itemQna(@RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<QnaResponse> qnaResponses = itemService.getAllQnaResponses(pageable);

        return new ResponseEntity<>(qnaResponses, HttpStatus.OK);
    }


    // 특정 qna 답글쓰기
    /**
     * http://localhost:8080/admin/item-list/3/qna/3/answer-qna
     *
     * qnaDto
     *{
     *     "answer": "example answer"
     * }
     */
    @PostMapping("/item-list/{itemId}/qna/{qnaId}/answer-qna")
    public ResponseEntity<?> addQna(@PathVariable(name = "itemId") long itemId,
                                    @PathVariable(name = "qnaId") long qnaId,
                                    @RequestBody QnaAnswerDto qnaAnswerDto) {

        QnaAnswerDto answerDto = itemService.addQnaAnswer(qnaAnswerDto, itemId, qnaId);

        return new ResponseEntity<>(answerDto, HttpStatus.OK);
    }

    // 전체 주문조회
    @GetMapping("/order-list")
    public ResponseEntity<?> allOrder(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "status", defaultValue = "ALL") String status) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<OrderDto> allOrderList;

        if (status.equals("ALL")) {
            allOrderList = orderService.findAllOrders(pageable);
        }
        else {
            allOrderList = orderService.findByStatus(status, pageable);
        }


        return new ResponseEntity<>(allOrderList, HttpStatus.OK);
    }

    // 주문상태 업데이트 ex) 배송중 -> 배송완료 - 관리자
    /**
     * http://localhost:8080/admin/order-list/3/update-status
     * orderDto
     * {
     *  "status" : "SHIPPED"
     * }
     *
     * status : CANCEL, PAID, SHIPPED(3가지만허용),
     */
    @PatchMapping("/order-list/{orderId}/update-status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable(name="orderId") Long orderId,
                                               @RequestBody OrderDto orderDto) {

        OrderDto updateOrderDto = orderService.updateOrderStatus(orderId, orderDto.getStatus());


        return new ResponseEntity<>(updateOrderDto, HttpStatus.OK);
    }






    //////////////여기부터 시작하면된다.t







    

    // 전체 배달정보 조회
    @GetMapping("/delivery-list")
    public ResponseEntity<?> allDelivery(@RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<DeliveryDto> allDelivery = deliveryService.findAllDelivery(pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(allDelivery, HttpStatus.OK);
    }

    // status별 배달정보 조회
    /**
     * http://localhost:8080/admin/delivery-list/ready
     *
     * status : ready, shipped, delivered, cancelled
     */
    @GetMapping("/delivery-list/category/{status}")
    public ResponseEntity<?> deliveryByStatus(@RequestParam(value = "page", defaultValue = "1") int page,
                                              @PathVariable(name="status") String status) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<DeliveryDto> statusDeliveryList = deliveryService.findByStatus(status, pageable);

        // 페이지 객체 자체를 전달해 전체 페이지 수, 총 요소 수, 현재 페이지 번호 등의 메타데이터도 클라이언트에 전달
        return new ResponseEntity<>(statusDeliveryList, HttpStatus.OK);
    }

    // 특정 배달정보 조회
    /**
     * http://localhost:8080/admin/delivery-list/3
     */
    @GetMapping("/delivery-list/{deliveryId}")
    public ResponseEntity<?> getDelivery(@PathVariable(name="deliveryId") Long deliveryId) {
        DeliveryDto deliveryDto = deliveryService.getDeliveryById(deliveryId);

        return new ResponseEntity<>(deliveryDto, HttpStatus.OK);
    }

    // 배달 정보 업데이트 ex) 배송중 -> 배송완료 - 관리자
    /**
     * http://localhost:8080/admin/orders/3/update-status
     * DeliveryDto
     * {
     *  "status" : "READY"
     * }
     *
     * status : READY, SHIPPED, DELIVERED, CANCLLED
     */
    @PatchMapping("/delivery-list/{deliveryId}/update-status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable(name="deliveryId") Long deliveryId,
                                               @RequestBody DeliveryDto deliveryDto) {

        deliveryService.updateDeliveryStatus(deliveryId, deliveryDto.getStatus());

        return new ResponseEntity<>("변경완료", HttpStatus.OK);
    }

}
