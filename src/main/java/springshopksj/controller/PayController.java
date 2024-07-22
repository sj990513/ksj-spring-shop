package springshopksj.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springshopksj.dto.*;
import springshopksj.service.PayService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
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
    @PostMapping("/{orderId}/virtual-account")
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
    @PostMapping("/{orderId}/ready")
    public KakaoReadyResponse readyToKakaoPay(@PathVariable(name="orderId") Long orderId,
                                              @RequestBody OrderRequest orderRequest) {

        return payService.kakaoPayReady(orderId, orderRequest.getPaymentDto());
    }

    // 카카오페이 결제성공
    @GetMapping("/{orderId}/success")
    public ResponseEntity afterPayRequest(@PathVariable(name="orderId") Long orderId,
                                          @RequestParam("pg_token") String pgToken) {

        KakaoApproveResponse kakaoApprove = payService.approveResponse(orderId, pgToken);

        return new ResponseEntity<>(kakaoApprove, HttpStatus.OK);
    }

    // 카카오페이 결제 진행중 취소
    @GetMapping("/cancel")
    public void cancel() {
        throw new RuntimeException("결제 취소");
    }

    // 카카오페이 결제 실패
    @GetMapping("/fail")
    public void fail() {
        throw new RuntimeException("결제 실패");
    }

    // 카카오페이 환불
    @PostMapping("/refund")
    public ResponseEntity refund() {

        KakaoCancelResponse kakaoCancelResponse = payService.kakaoCancel();

        return new ResponseEntity<>(kakaoCancelResponse, HttpStatus.OK);
    }
}
