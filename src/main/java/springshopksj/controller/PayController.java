package springshopksj.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.*;
import springshopksj.service.PayService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payment")
public class PayController {

    private final PayService payService;


    // 기본결제 (가상계좌이체)
    /**
     * orderRequest
     *       {
     *           "paymentDto": {
     *               "paymentMethod": "VIRTUAL_ACCOUNT",
     *               "amount": 60000
     *           }
     *       }
     */
    @PostMapping("/virtual-account/{orderId}")
    public ResponseEntity<?> payment(@PathVariable(name="orderId") Long orderId,
                                     @RequestBody OrderRequest orderRequest) {

        PaymentDto paymentDto = payService.createPayment(orderId, orderRequest.getPaymentDto());

        return new ResponseEntity<>(paymentDto, HttpStatus.OK);
    }

    // 카카오페이 결제요청
    /**
     * orderRequest
     *       {
     *           "paymentDto": {
     *               "paymentMethod": "KAKAO_PAY",
     *               "amount": 120000,
     *               "itemName" : "아이템1 외 2건"
     *           }
     *       }
     */
    @PostMapping("/ready/{orderId}")
    public KakaoReadyResponse readyToKakaoPay(@PathVariable(name="orderId") Long orderId,
                                              @RequestBody OrderRequest orderRequest,
                                              @RequestHeader("access") String accessToken) {

        return payService.kakaoPayReady(orderId, orderRequest.getPaymentDto(), accessToken);
    }

    // 카카오페이 결제성공
    @GetMapping("/{orderId}/success")
    public ResponseEntity afterPayRequest(@PathVariable(name="orderId") Long orderId,
                                          @RequestParam("pg_token") String pgToken) {

        KakaoApproveResponse kakaoApprove = payService.approveResponse(orderId, pgToken);

        return new ResponseEntity<>(kakaoApprove, HttpStatus.OK);
    }

    // 카카오페이 결제 진행중 취소
    @GetMapping("/{orderId}/cancel")
    public ResponseEntity cancel(@PathVariable(name="orderId") Long orderId) {

        PaymentDto paymentDto = payService.kakaoCancel(orderId);

        return new ResponseEntity<>(paymentDto, HttpStatus.OK);
    }

    // 카카오페이 결제 실패
    @GetMapping("/{orderId}/fail")
    public ResponseEntity fail() {
        return new ResponseEntity<>("결제 실패", HttpStatus.OK);
    }

    // 카카오페이 환불
    @PostMapping("/{orderId}/refund")
    public ResponseEntity refund() {

        KakaoCancelResponse kakaoCancelResponse = payService.kakaoRefund();

        return new ResponseEntity<>(kakaoCancelResponse, HttpStatus.OK);
    }
}
