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
import springshopksj.service.ItemService;
import springshopksj.service.MemberService;
import springshopksj.utils.Constants;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final MemberService memberService;

    //전체 아이템목록 조회 (페이징 처리), 검색어 존재할시 검색된 아이템들만 전달 - 로그인 불필요
    /**
     * http://localhost:8080/items/item-list?page=3&search=example
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

    //카테고리별 아이템 조회 (페이징 처리) - 로그인 불필요
    /**
     * http://localhost:8080/items/item-list/pants?page=3&search=example
     * category : top, pants, cap, shoes
     */
    @GetMapping("/item-list/{category}")
    public ResponseEntity<?> findByCategoryItemList(@PathVariable(name = "category") String category,
                                                    @RequestParam(value = "search", required = false) String search,
                                                    @RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        //검색어 존재할시
        if (search != null) {
            Page<ItemDto> findByCategoryAndSearch = itemService.searchItemsByCategoryAndKeyword(category, search, pageable);
            return new ResponseEntity<>(findByCategoryAndSearch, HttpStatus.OK);
        }

        Page<ItemDto> findByCategory = itemService.findByCategory(category, pageable);

        return new ResponseEntity<>(findByCategory, HttpStatus.OK);
    }

    //아이템 추가 - 관리자만 가능
    /**
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
    @PostMapping("/add-item")
    public ResponseEntity<?> addItem(@RequestBody ItemDto itemDto) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String message = itemService.addItem(itemDto, memberDto);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }


    //아이템 상세보기 - 로그인필요, 리뷰 리스트도 추가해서 전달
    /**
     * http://localhost:8080/items/3
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<?> itemDetail(@PathVariable(name = "itemId") long itemId) {

        ItemDetailDto itemDetailDto = itemService.findItemDetail(itemId);

        return new ResponseEntity<>(itemDetailDto, HttpStatus.OK);
    }

    // 상품에 대한 모든 리뷰
    /**
     * http://localhost:8080/items/3/review
     */
    @GetMapping("/{itemId}/review")
    public ResponseEntity<?> itemReview(@PathVariable(name = "itemId") long itemId,
                                        @RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);


        Page<ReviewDto> allReview = itemService.findAllReview(itemId, pageable);

        return new ResponseEntity<>(allReview, HttpStatus.OK);
    }

    // 상품 리뷰 생성 (배송완료 제품만 리뷰 가능)
    /**
     * http://localhost:8080/items/3/review/add-review
     *
     * reviewDto
     * {
     *     "rating": 5,
     *     "comment": "example12345"
     * }
     */
    @PostMapping("/{itemId}/review/add-review")
    public ResponseEntity<?> addReview(@PathVariable(name = "itemId") long itemId,
                                       @RequestBody ReviewDto reviewDto) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        ReviewDto review = itemService.addReview(memberDto, itemId, reviewDto);

        return new ResponseEntity<>(review, HttpStatus.OK);
    }


    // 리뷰삭제 - 리뷰 작성자 본인 + 관리자 삭제가능하게, admincontroller에도 리뷰삭제 추가
    /**
     * http://localhost:8080/items/3/review/6/delete-review
     */
    @DeleteMapping("/{itemId}/review/{reviewId}/delete-review")
    ResponseEntity<?> deleteReview(@PathVariable(name = "itemId") long itemId,
                                   @PathVariable(name = "reviewId") long reviewId) {
        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String message = itemService.deleteReview(memberDto, itemId, reviewId);

        if (message.equals("삭제성공"))
            return new ResponseEntity<>(message, HttpStatus.OK);

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }


    // 상품에 대한 모든 qna - (qna답변 존재할시 qna답변도 같이 출력)
    /**
     * http://localhost:8080/items/3/qna
     */
    @GetMapping("/{itemId}/qna")
    public ResponseEntity<?> getQnaList(@PathVariable(name = "itemId") long itemId,
                                        @RequestParam(value = "page", defaultValue = "1") int page) {

        PageRequest pageable = PageRequest.of(page-1 , Constants.PAGE_SIZE);

        Page<QnaResponse> qnaResponses = itemService.getQnaResponses(itemId, pageable);

        return new ResponseEntity<>(qnaResponses, HttpStatus.OK);
    }

    // 상품에 대한 qna 등록
    /**
     * http://localhost:8080/items/3/qna/add-qna
     *
     * qnaDto
     *{
     *     "title": "example title",
     *     "content": "example content"
     * }
     */
    @PostMapping("/{itemId}/qna/add-qna")
    public ResponseEntity<?> addQna(@PathVariable(name = "itemId") long itemId,
                                    @RequestBody QnaDto qnaDto) {

        //로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        QnaDto saveQnaDto = itemService.addQna(qnaDto, itemId, memberDto);

        return new ResponseEntity<>(saveQnaDto, HttpStatus.OK);
    }


    // 아이템 수정 - 로그인필요
    /**
     * http://localhost:8080/items/3/update
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
    @PatchMapping("/{itemId}/update")
    public ResponseEntity<?> updateItem(@PathVariable(name = "itemId") long itemId,
                                        @RequestBody ItemDto itemDto) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        ItemDto updateItem = itemService.updateItem(itemId, itemDto, memberDto);

        return new ResponseEntity<>(updateItem, HttpStatus.OK);
    }


    // 상품삭제 - 로그인필요 (작성자 본인만 삭제가능)
    /**
     * http://localhost:8080/items/3/delete-item
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
    @DeleteMapping("/{itemId}/delete-item")
    public ResponseEntity<?> deleteItem(@PathVariable(name = "itemId") long itemId) {

        //현재 로그인중인 사용자
        MemberDto memberDto = memberService.fidnByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        String message = itemService.deleteItem(itemId, memberDto);

        if(message.equals("삭제성공"))
            return new ResponseEntity<>(message, HttpStatus.OK);

        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

}













