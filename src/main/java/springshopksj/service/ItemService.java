package springshopksj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import springshopksj.dto.*;
import springshopksj.entity.*;
import springshopksj.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;
    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;


    private ItemDto convertToItemDto(Item item) {
        ItemDto itemDto = modelMapper.map(item, ItemDto.class);
        itemDto.setUserID(item.getMember().getID());
        return itemDto;
    }

    private ReviewDto convertToReviewDto(Review review) {
        ReviewDto reviewDto = modelMapper.map(review, ReviewDto.class);
        reviewDto.setItemID(review.getItem().getID());
        reviewDto.setUserID(review.getMember().getID());
        return reviewDto;
    }

    private QnaDto convertToQnaDto(Qna qna) {
        QnaDto qnaDto = modelMapper.map(qna, QnaDto.class);
        qnaDto.setItemID(qna.getItem().getID());
        qnaDto.setUserID(qna.getMember().getID());
        return qnaDto;
    }

    private QnaAnswerDto convertToQnaAnswerDto(QnaAnswer qnaAnswer) {
        QnaAnswerDto qnaAnswerDto = modelMapper.map(qnaAnswer, QnaAnswerDto.class);
        qnaAnswerDto.setQnaID(qnaAnswer.getQna().getID());
        return qnaAnswerDto;
    }

    //모든 아이템 조회 (페이징 처리)
    public Page<ItemDto> findAllItems(Pageable pageable) {

        Page<Item> findItems = itemRepository.findAll(pageable);

        return findItems.map(this::convertToItemDto);
    }

    // 카테고리별 조회 (페이징 처리)
    public Page<ItemDto> findByCategory(String category, Pageable pageable) {

        Page<Item> findByCategory = itemRepository.findByCategory(category, pageable);

        return findByCategory.map(this::convertToItemDto);
    }

    // 아이템 검색
    public Page<ItemDto> findBySearch(String keyword, Pageable pageable) {

        Page<Item> findBySearch = itemRepository.findByItemnameContaining(keyword, pageable);

        return findBySearch.map(this::convertToItemDto);
    }

    // 카테고리내에서 아이템 검색
    public Page<ItemDto> searchItemsByCategoryAndKeyword(String category, String keyword, Pageable pageable) {

        Page<Item> findByCategoryAndSearch = itemRepository.findByCategoryAndItemnameContaining(category, keyword, pageable);

        return findByCategoryAndSearch.map(this::convertToItemDto);
    }

    // itemId로 아이템값 찾기
    public ItemDetailDto findItemDetail(long itemId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        ItemDto itemDto = convertToItemDto(item);

        List<Review> reviewList = reviewRepository.findByItemID(itemId);
        List<ReviewDto> reviewListDto = reviewList.stream().map(this::convertToReviewDto).collect(Collectors.toList());

        ItemDetailDto itemDetailDto = ItemDetailDto.builder()
                .itemDto(itemDto)
                .reviewListDto(reviewListDto)
                .build();

        return itemDetailDto;
    }

    // 상품에 대한 모든 리뷰 - 페이징
    public Page<ReviewDto> findAllReview(long itemId, Pageable pageable) {

        Page<Review> findReviews = reviewRepository.findByItemID(itemId, pageable);

        return findReviews.map(this::convertToReviewDto);
    }

    // 리뷰추가 - 상품이 배송완료 상태에 해당되는 상품만 리뷰작성가능
    public ReviewDto addReview(MemberDto memberDto, long itemId, ReviewDto reviewDto) {

        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));


        List<OrderItem> orderItems = orderRepository.findDeliveredOrderItemsByMemberAndItem(member.getID(), itemId);

        // 배송완료된 상품만 리뷰작성가능
        if (orderItems.isEmpty()) {
            throw new RuntimeException("리뷰를 작성할 권한이 없습니다. 상품이 배송 완료 상태가 아닙니다.");
        }


        Review review = Review.builder()
                .comment(reviewDto.getComment())
                .rating(reviewDto.getRating())
                .item(item)
                .member(member)
                .build();

        reviewRepository.save(review);

        return convertToReviewDto(review);
    }

    // 리뷰 삭제 - 리뷰 작성자 본인 + 관리자 삭제가능
    public String deleteReview(MemberDto memberDto, long itemId, long reviewId) {

        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을수 없습니다."));

        String message;

        // 상품에 대한 리뷰여야되고, 본인이 작성한 리뷰여야되거나 관리자일경우 삭제가능
        if ( (review.getItem().getID() == item.getID() && review.getMember().getID() == member.getID()) || member.getRole().equals(Member.Role.ROLE_ADMIN)) {
            reviewRepository.delete(review);
            message = "삭제성공";
        } else  {
            message = "해당 리뷰를 삭제할 권한이 없습니다.";
        }
        return  message;
    }

    // 상품에 대한 모든 qna와 qna_answer 리스트 - 페이징
    public Page<QnaResponse> getQnaResponses(long itemId, Pageable pageable) {
        Page<Qna> qnaPage = qnaRepository.findByItemID(itemId, pageable);

        return qnaPage.map(qna -> {
            QnaDto qnaDto = convertToQnaDto(qna);
            QnaAnswer qnaAnswer = qnaAnswerRepository.findByQnaID(qna.getID());
            QnaAnswerDto qnaAnswerDto = null;

            if (qnaAnswer != null) {
                qnaAnswerDto = convertToQnaAnswerDto(qnaAnswer);
            }

            return new QnaResponse(qnaDto, qnaAnswerDto);
        });
    }


    // qna등록
    public QnaDto addQna(QnaDto qnaDto, long itemId, MemberDto memberDto) {

        Member member = memberRepository.findById(memberDto.getID())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        Qna qna = Qna.builder()
                .title(qnaDto.getTitle())
                .content(qnaDto.getContent())
                .createdate(LocalDateTime.now())
                .item(item)
                .member(member)
                .build();

        qnaRepository.save(qna);

        return convertToQnaDto(qna);
    }

    // qna 답변 등록 - 관리자만 가능, 답변은 qna당 하나만
    public QnaAnswerDto addQnaAnswer(QnaAnswerDto qnaAnswerDto, long itemId, long qnaId) {

        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("QnA를 찾을수 없습니다."));

        //qnaId가 item에 대한 qnaId인지 판별
        if (!(qna.getItem().getID() == itemId)) {
            throw new RuntimeException("QnA가 해당 상품에 대한 것이 아닙니다.");
        }

        Optional<QnaAnswer> findQna = qnaAnswerRepository.findOptionalByQnaID(qnaId);
        // 이미 답변이 존재할시
        if ( !findQna.isEmpty() )
            throw new RuntimeException("이미 질문에 대한 답변이 존재합니다.");


        QnaAnswer qnaAnswer = QnaAnswer.builder()
                .answer(qnaAnswerDto.getAnswer())
                .createdate(LocalDateTime.now())
                .qna(qna)
                .build();

        qnaAnswerRepository.save(qnaAnswer);
        
        return convertToQnaAnswerDto(qnaAnswer);
    }


    // 아이템 수정
    public ItemDto updateItem(long itemId, ItemDto updateItemDto, MemberDto memberDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        // 작성자 본인만 아이템 디테일 수정가능
        if ( item.getMember().getID() == memberDto.getID() ) {

            item.setItemname(updateItemDto.getItemname());
            item.setPrice(updateItemDto.getPrice());
            item.setStock(updateItemDto.getStock());
            item.setCategory(updateItemDto.getCategory());
            item.setDescription(updateItemDto.getDescription());
            item.setImageUrl(updateItemDto.getImageUrl());

            itemRepository.save(item);
            return convertToItemDto(item);
        } else {
            throw new RuntimeException("상품을 수정할 권한이 없습니다.");
        }
    }

    //아이템 추가 - 관리자만 가능
    public String addItem(ItemDto itemDto, MemberDto memberDto) {

        Member member = memberRepository.findByUsername(memberDto.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));

        if(member.getRole() != Member.Role.ROLE_ADMIN)
            throw new RuntimeException("상품추가는 관리자만 가능합니다.");

        Item item = Item.builder()
                .itemname(itemDto.getItemname())
                .price(itemDto.getPrice())
                .stock(itemDto.getStock())
                .category(itemDto.getCategory())
                .description(itemDto.getDescription())
                .imageUrl(itemDto.getImageUrl())
                .createDate(LocalDateTime.now())
                .member(member)
                .build();

        itemRepository.save(item);

        return "아이템 추가 성공";
    }

    // 아이템삭제
    public String deleteItem(long itemId, MemberDto memberDto) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을수 없습니다."));

        String role = String.valueOf(memberDto.getRole());

        //글작성자나 관리자일경우 허용
        if ( item.getMember().getID() == memberDto.getID() || role.equals("ROLE_ADMIN") ) {
            itemRepository.delete(item);
            return "삭제성공";
        }

        return "삭제할 권한이 없습니다.";
    }
}
