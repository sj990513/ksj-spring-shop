package springshopksj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import springshopksj.dto.KakaoApproveResponse;
import springshopksj.dto.KakaoCancelResponse;
import springshopksj.dto.KakaoReadyResponse;
import springshopksj.dto.PaymentDto;
import springshopksj.entity.Member;
import springshopksj.entity.Order;
import springshopksj.entity.Payment;
import springshopksj.repository.MemberRepository;
import springshopksj.repository.OrderRepository;
import springshopksj.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayService {

    static final String cid = "TC0ONETIME"; // 가맹점 테스트 코드
    static String partner_order_id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    static String partner_user_id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    @Value("${kakao.admin-key}") String admin_Key;
    private KakaoReadyResponse kakaoReady;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    private PaymentDto convertToPaymentDto(Payment payment) {
        PaymentDto paymentDto = modelMapper.map(payment, PaymentDto.class);
        paymentDto.setOrderID(payment.getOrder().getID());
        return paymentDto;
    }

    // 기본결제 (가상계좌)
    public PaymentDto createPayment(long orderId, PaymentDto paymentDto) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을수 없습니다."));

        // 결제 금액 불일치시
        if (order.getTotalprice() != paymentDto.getAmount())
            throw new RuntimeException("결제 금액이 일치하지 않습니다.");

        //주문상태일때만 결제가능
        if (!order.getStatus().equals(Order.OrderStatus.ORDERED))
            throw new RuntimeException("결제 가능한 상태가 아닙니다.");

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .order(order)
                .paymentmethod(paymentDto.getPaymentMethod())
                .amount(paymentDto.getAmount())
                .paymentdate(LocalDateTime.now())
                .build();

        // 결제완료로 변경
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);

        paymentRepository.save(payment);

        PaymentDto updatePaymentDto = convertToPaymentDto(payment);

        return updatePaymentDto;
    }

    // 카카오페이 ready
    public KakaoReadyResponse kakaoPayReady(long orderId, PaymentDto paymentDto) {

        Member member = memberRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을수 없습니다."));

        //주문상태일때만 결제가능
        if (!order.getStatus().equals(Order.OrderStatus.ORDERED))
            throw new RuntimeException("결제 가능한 상태가 아닙니다.");

        // 결제 금액 불일치시
        if (order.getTotalprice() != paymentDto.getAmount())
            throw new RuntimeException("결제 금액이 일치하지 않습니다.");

        // 카카오페이 요청 양식
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        parameters.add("cid", cid);
        parameters.add("partner_order_id", partner_order_id + orderId);
        parameters.add("partner_user_id", partner_user_id + member.getID());
        parameters.add("item_name", paymentDto.getItemName());
        parameters.add("quantity", "1");
        parameters.add("total_amount", String.valueOf(order.getTotalprice()));
        parameters.add("vat_amount", "0");
        parameters.add("tax_free_amount", "0");
        parameters.add("approval_url", "http://localhost:8080/payment/" + orderId + "/success"); // 성공 시 redirect url
        parameters.add("cancel_url", "http://localhost:8080/payment/cancel");                   // 취소 시 redirect url
        parameters.add("fail_url", "http://localhost:8080/payment/fail");                       // 실패 시 redirect url

        // 파라미터, 헤더
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();

        kakaoReady = restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/ready",
                requestEntity,
                KakaoReadyResponse.class);

        // 결제 정보 생성, 저장
        Payment payment = Payment.builder()
                .order(order)
                .paymentmethod(paymentDto.getPaymentMethod())
                .amount(paymentDto.getAmount())
                .paymentdate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return kakaoReady;
    }


    // 카카오 페이 결제완료 승인
    public KakaoApproveResponse approveResponse(long orderId, String pgToken) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Member> member = memberRepository.findByUsername(username);

        // 카카오 요청
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", cid);
        parameters.add("tid", kakaoReady.getTid());
        parameters.add("partner_order_id", partner_order_id + orderId);
        parameters.add("partner_user_id", partner_user_id + member.get().getID());
        parameters.add("pg_token", pgToken);

        // 파라미터, 헤더
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();

        KakaoApproveResponse approveResponse = restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/approve",
                requestEntity,
                KakaoApproveResponse.class);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을수 없습니다."));


        // 결제완료로 변경
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);

        return approveResponse;
    }

    /**
     * 카카오 결제 환불
     */
    public KakaoCancelResponse kakaoCancel() {

        // 카카오페이 요청
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", cid);
        parameters.add("tid", "환불할 결제 고유 번호");
        parameters.add("cancel_amount", "환불 금액");
        parameters.add("cancel_tax_free_amount", "환불 비과세 금액");
        parameters.add("cancel_vat_amount", "환불 부가세");

        // 파라미터, 헤더
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        // 외부에 보낼 url
        RestTemplate restTemplate = new RestTemplate();

        KakaoCancelResponse cancelResponse = restTemplate.postForObject(
                "https://kapi.kakao.com/v1/payment/cancel",
                requestEntity,
                KakaoCancelResponse.class);

        return cancelResponse;
    }


    /**
     * 카카오 요구 헤더값
     */
    private HttpHeaders getHeaders() {
        org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();

        String auth = "KakaoAK " + admin_Key;

        httpHeaders.set("Authorization", auth);
        httpHeaders.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        return httpHeaders;
    }
}
