package springshopksj.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import springshopksj.dto.*;
import springshopksj.entity.*;
import springshopksj.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    // OrderItemDto맵핑 메소드
    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = modelMapper.map(orderItem, OrderItemDto.class);
        orderItemDto.setOrderID(orderItem.getOrder().getID());
        orderItemDto.setItemID(orderItem.getItem().getID());
        return orderItemDto;
    }

    // OrderDto맵핑 메소드
    private OrderDto convertToOrderDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setUserID(order.getMember().getID());
        return orderDto;
    }

    private DeliveryDto convertToDeliveryDto(Delivery delivery) {
        DeliveryDto deliveryDto = modelMapper.map(delivery, DeliveryDto.class);
        deliveryDto.setOrderID(delivery.getOrder().getID());
        return deliveryDto;
    }

    private PaymentDto convertToPaymentDto(Payment payment) {
        PaymentDto paymentDto = modelMapper.map(payment, PaymentDto.class);
        paymentDto.setOrderID(payment.getOrder().getID());
        return paymentDto;
    }


    // 장바구니 조회
    public List<OrderItemDto> findAllCart(MemberDto memberDto) {
        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 기존 장바구니(PENDING 상태)를 찾음
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            // 기존의 장바구니가 없을 시 장바구니 생성 (PENDING)
            order = Order.builder()
                    .member(member)
                    .status(Order.OrderStatus.PENDING)
                    .orderdate(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }


        List<OrderItem> cart = orderItemRepository.findByOrderID(order.getID());

        List<OrderItemDto> cartDto = cart.stream().map(this::convertToOrderItemDto).collect(Collectors.toList());

        return cartDto;
    }

    // 장바구니 품목 추가(생성)
    public List<OrderItemDto> addCart(MemberDto memberDto, List<OrderItemDto> orderItems) {

        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 기존 장바구니(PENDING 상태)를 찾음
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            // 기존의 장바구니가 없을 시 장바구니 생성 (PENDING)
            order = Order.builder()
                    .member(member)
                    .status(Order.OrderStatus.PENDING)
                    .orderdate(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }

        long totalprice = order.getTotalprice();

        // 장바구니에 아이템 추가
        for (OrderItemDto orderItemDto : orderItems) {

            Item item = itemRepository.findById(orderItemDto.getItemID())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

            if (item.getStock() < orderItemDto.getCount()) {
                throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .orderprice(item.getPrice())
                    .count(orderItemDto.getCount())
                    .build();

            totalprice += (orderItem.getCount() * orderItem.getOrderprice());

            orderItemRepository.save(orderItem);
        }

        order.setTotalprice(totalprice);
        orderRepository.save(order);

        List<OrderItem> cart = orderItemRepository.findByOrderID(order.getID());
        List<OrderItemDto> cartDto = cart.stream().map(this::convertToOrderItemDto).collect(Collectors.toList());

        return cartDto;
    }

    // 장바구니 품목 삭제
    public List<OrderItemDto> deleteCartItem(MemberDto memberDto, long order_itemID) {


        int deleteprice = 0;
        
        // 장바구니 탐색
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            throw new RuntimeException("삭제할 장바구니가 존재하지 않습니다.");
        }

        List<OrderItem> findByOrderId = orderItemRepository.findByOrderID(order.getID());

        // 본인의 장바구니안에 존재하는 품목만 삭제가능
        if (! findByOrderId.stream()
                .anyMatch( orderItem -> orderItem.getID() == order_itemID))
            throw new RuntimeException("해당 장바구니 상품을 삭제할 권한이 없습니다.");

        
        // 장바구니 상품 삭제
        OrderItem orderItem = orderItemRepository.findById(order_itemID)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        orderItemRepository.delete(orderItem);

        // 장바구니 가격 조정
        deleteprice = orderItem.getCount() * orderItem.getOrderprice();
        order.setTotalprice(order.getTotalprice() - deleteprice);
        orderRepository.save(order);

        
        List<OrderItem> cart = orderItemRepository.findByOrderID(order.getID());
        List<OrderItemDto> cartDto = cart.stream().map(this::convertToOrderItemDto).collect(Collectors.toList());

        return cartDto;
    }

    // 장바구니 삭제
    public void deleteCart(MemberDto memberDto) {

        // 장바구니 탐색
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElse(null);

        if (order == null) {
            throw new RuntimeException("삭제할 장바구니가 존재하지 않습니다.");
        }

        orderRepository.delete(order);
    }

    
    // 장바구니에서 결제
    public OrderDto createOrderFromCart(MemberDto memberDto, DeliveryDto deliveryDto) {
        
        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        // 장바구니 불러오기
        Order order = orderRepository.findByMemberIDAndStatus(memberDto.getID(), Order.OrderStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("장바구니가 존재하지 않습니다."));

        // 장바구니안에 존재하는 상품들
        List<OrderItem> orderItems = orderItemRepository.findByOrderID(order.getID());

        if (orderItems.isEmpty()) {
            throw new RuntimeException("장바구니에 상품이 존재하지 않습니다.");
        } else {
            for (OrderItem orderItem : orderItems) {
                
                Item item = itemRepository.findById(orderItem.getItem().getID())
                        .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

                if (item.getStock() < orderItem.getCount()) {
                    throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
                }

                // 주문개수만큼 재고 삭제
                item.setStock(item.getStock() - orderItem.getCount());
                itemRepository.save(item);
            }
        }

        // 배송 정보 생성
        Delivery delivery = Delivery.builder()
                .order(order)
                .status(Delivery.DeliveryStatus.READY)
                .address(deliveryDto.getAddress())
                .build();
        deliveryRepository.save(delivery);

        // 주문완료로 상태 업데이트
        order.setStatus(Order.OrderStatus.ORDERED);
        orderRepository.save(order);

        OrderDto orderDto = convertToOrderDto(order);

        return orderDto;
    }

    // 아이템 디테일에서 개별주문(즉시주문) 생성
    public OrderDto createOrder(MemberDto memberDto, List<OrderItemDto> orderItems, DeliveryDto deliveryDto) {

        // 주문 요청한 사용자 (현재 로그인한 사용자)
        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        long totalprice = 0;

        // 재고 확인 및 총 가격 계산
        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderItemDto orderItemDto : orderItems) {
            Item item = itemRepository.findById(orderItemDto.getItemID())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

            if (item.getStock() < orderItemDto.getCount()) {
                throw new RuntimeException("해당 상품의 재고가 남아있지 않습니다.: " + item.getItemname());
            }

            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .orderprice(item.getPrice())
                    .count(orderItemDto.getCount())
                    .build();

            orderItemList.add(orderItem);
            totalprice += (orderItem.getCount() * orderItem.getOrderprice());
        }

        // 모든 검사가 통과된 후 주문 데이터 생성
        Order order = Order.builder()
                .member(member)
                .status(Order.OrderStatus.ORDERED)
                .orderdate(LocalDateTime.now())
                .totalprice(totalprice)
                .build();

        orderRepository.save(order);

        // 주문 항목 생성 및 재고 업데이트
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);

            Item item = orderItem.getItem();
            item.setStock(item.getStock() - orderItem.getCount());
            itemRepository.save(item);
        }

        // 배송 정보 생성
        Delivery delivery = Delivery.builder()
                .order(order)
                .status(Delivery.DeliveryStatus.READY)
                .address(deliveryDto.getAddress())
                .build();

        deliveryRepository.save(delivery);

        OrderDto orderDto = convertToOrderDto(order);

        return orderDto;
    }

    // userId로 order찾기
    public Page<OrderDto> getOrdersByUserId(MemberDto memberDto, Pageable pageable) {

        Page<Order> orders = orderRepository.findByMemberID(memberDto.getID(), pageable);

        return orders.map(this::convertToOrderDto);
    }

    // orderId로 모든 order 디테일 찾기 - 주문상세보기 (order, orderItems, payment, delivery)
    public OrderResponse findOrderDetailByOrderId(MemberDto memberDto, long orderId) {

        String message = "주문에 해당하는 상세정보를 찾을수 없습니다.";

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(message));

        if (order.getMember().getID() != memberDto.getID())
            throw new RuntimeException("해당 주문조회의 권한이 없습니다.");

        List<OrderItem> orderItems = orderItemRepository.findByOrderID(orderId);


        Payment payment = paymentRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException(message));

        Delivery delivery = deliveryRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException(message));

        OrderResponse orderResponse = OrderResponse.builder()
                .orderDto(convertToOrderDto(order))
                .orderItemDtos(orderItems.stream().map(this::convertToOrderItemDto).collect(Collectors.toList()))
                .paymentDto(convertToPaymentDto(payment))
                .deliveryDto(convertToDeliveryDto(delivery))
                .build();

        return orderResponse;
    }

    //모든 order 조회 - 페이징처리
    public Page<OrderDto> findAllOrders(Pageable pageable) {

        Page<Order> allOrders = orderRepository.findAll(pageable);

        return allOrders.map(this::convertToOrderDto);
    }

    // order status별 조회 - 페이징 처리
    public Page<OrderDto> findByStatus(String status, Pageable pageable) {

        Order.OrderStatus orderStatus;

        switch (status) {
            case "ordered":
                orderStatus = Order.OrderStatus.ORDERED;
                break;

            case "paid":
                orderStatus = Order.OrderStatus.PAID;
                break;

            case "cancle":
                orderStatus = Order.OrderStatus.CANCEL;
                break;

            case "cancelled":
                orderStatus = Order.OrderStatus.CANCELLED;
                break;

            case "shipped":
                orderStatus = Order.OrderStatus.SHIPPED;
                break;

            case "delivered":
                orderStatus = Order.OrderStatus.DELIVERED;
                break;

            default:
                orderStatus = Order.OrderStatus.ORDERED;
        }

        Page<Order> findByCategory = orderRepository.findByStatus(orderStatus, pageable);

        return findByCategory.map(this::convertToOrderDto);
    }

    // order status별 조회 - 페이징 처리 x - 관리자페이지에서 사용
    public List<OrderDto> findByStatusList(Order.OrderStatus status) {

        List<Order> findByCategory = orderRepository.findByStatus(status);

        return findByCategory.stream().map(this::convertToOrderDto).collect(Collectors.toList());
    }

    // 특정 주문 조회 (관리자페이지에서 사용)
    public OrderDto getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        OrderDto orderDto = convertToOrderDto(order);

        return  orderDto;
    }


    // order캔슬 (사용자가 요청)
    public void cancelOrder(long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        // 결제전 주문 상태때맨 취소가능
        if (order.getStatus() != Order.OrderStatus.ORDERED) {
            throw new RuntimeException("주문 취소가 불가능합니다.");
        }
        order.setStatus(Order.OrderStatus.CANCEL);
        orderRepository.save(order);
    }

    // order status 업데이트 status : CANCELLED, SHIPPED, DELIVERED (3가지만허용), - 관리자
    public OrderDto updateOrderStatus(long orderId, Order.OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 주문을 찾을수 없습니다."));

        Delivery delivery = deliveryRepository.findByOrderID(orderId)
                .orElseThrow(() -> new RuntimeException("해당하는 배송정보를 찾을수 없습니다."));


        // 주문취소허용 들어오면 delivery도 같이 주문취소로 변경 (order상태 CANCLE일때만 허용)
        if (status.equals(Order.OrderStatus.CANCELLED)) {

            // 취소된 주문이 아니거나, 배송전 상품이 아니면 취소 불가능
            if ( !order.getStatus().equals(Order.OrderStatus.CANCEL) || !delivery.getStatus().equals(Delivery.DeliveryStatus.READY)) {
                throw new RuntimeException("주문 취소가 불가능합니다.");
            }

            List<OrderItem> orderItems = orderItemRepository.findByOrderID(orderId);

            for (OrderItem orderItem : orderItems) {
                Item item = itemRepository.findById(orderItem.getItem().getID())
                        .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

                // 재고복구
                item.setStock(item.getStock() + orderItem.getCount());
                itemRepository.save(item);

            }
            // 배송 취소로 변경
            delivery.setStatus(Delivery.DeliveryStatus.CANCELLED);
        }

        // 배송중으로 들어오면 delivery도 같이 배송중으로 변경 (order상태 PAID일때만 허용)
        else if (status.equals(Order.OrderStatus.SHIPPED)) {
            if (order.getStatus().equals(Order.OrderStatus.PAID)) {
                delivery.setStatus(Delivery.DeliveryStatus.SHIPPED);
            } else {
                throw new RuntimeException("결제가 완료되지 않았습니다. 결제확인후 배송이 가능합니다.");
            }
        }

        // 배송완료로 들어오면 delivery도 같이 배송완료로 변경 (order상태 SHIPPED일때만 허용)
        else if (status.equals(Order.OrderStatus.DELIVERED)) {
            if (order.getStatus().equals(Order.OrderStatus.SHIPPED)) {
                delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
            } else {
                throw new RuntimeException("배송중인 상품이 아닙니다.");
            }
        }

        else {
            throw new RuntimeException("허용되지 않는 상태변경입니다.");
        }

        order.setStatus(status);

        orderRepository.save(order);
        deliveryRepository.save(delivery);

        return convertToOrderDto(order);
    }
}
