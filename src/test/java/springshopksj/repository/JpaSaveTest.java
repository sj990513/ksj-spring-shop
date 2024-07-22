package springshopksj.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import springshopksj.entity.Qna;
import springshopksj.entity.Item;
import springshopksj.entity.Member;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@DataJpaTest
@Slf4j
public class JpaSaveTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private QnaRepository qnaRepository;

    @BeforeEach
    void 기본데이터삽입() {
        Member member1 = Member.builder()
                .password("qwer123456")
                .username("qwer")
                .email("qwer1234@gmail.com")
                .phone("010-3164-8408")
                .role(Member.Role.ROLE_USER)
                .createDate(LocalDateTime.now())
                .build();

        Member member2 = Member.builder()
                .password("asdf1234")
                .username("2222222")
                .email("1234@naver.com")
                .phone("010-1234-5555")
                .role(Member.Role.ROLE_USER)
                .createDate(LocalDateTime.now())
                .build();

        Member member3 = Member.builder()
                .password("zzzzzzzzzzzzxc")
                .username("2265876587658")
                .email("643834@naver.com")
                .phone("240-5784-5555")
                .role(Member.Role.ROLE_USER)
                .createDate(LocalDateTime.now())
                .build();
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Item item1 = Item.builder()
                .itemname("item1")
                .price(10000)
                .stock(50)
                .category("옷")
                .description("이옷은 가나다라마바아자차타카파하")
                .imageUrl("img/1234/56787")
                .member(member3)
                .createDate(LocalDateTime.now())
                .build();
        itemRepository.save(item1);

        Qna qna1 = Qna.builder()
                .content("가나다라마바사아자차")
                .title("member2가쓴제목")
                .member(member2)
                .build();

        qnaRepository.save(qna1);

    }
    
    @Test
    void 멤버데이터테스트() {

        List<Member> members = memberRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for(Member m : members) {
            String formattedCreateDate = m.getCreateDate().format(formatter);
            log.info("m.getRole() = " + m.getRole()
                    + "\nm.getUsername() = " + m.getUsername()
                    + "시간 = " + formattedCreateDate
                    + "ID = " + m.getID()
                    + "\nm.getEmail() = " + m.getEmail() + "\n\n");
        }
    }

    @Test
    void 아이템데이터테스트() {

        List<Item> items = itemRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for(Item i : items) {
            String formattedCreateDate = i.getCreateDate().format(formatter);
            log.info("i.getItemName() = " + i.getItemname()
                    + "\ni.getCreateDate() = " + i.getCreateDate()
                    + "\ni.getStock() = " + i.getStock()
                    + "\ni.getUserID() = " + i.getMember().getID());
        }
    }

    @Test
    void 자유게시판테스트() throws InterruptedException {

        Thread.sleep(3000);

        Qna qna = qnaRepository.findAll().get(0);

        qna = qna.toBuilder().title("제목을 이거로바꾸겠다").build();
        qnaRepository.save(qna);


        List<Qna> qnas = qnaRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for(Qna b : qnas) {
            String formattedCreateDate = b.getCreatedate().format(formatter);
            String formattedModifyDate = b.getModifydate().format(formatter);
            log.info("b.getID() = " + b.getID()
                    + "\nb.getContent() = " + b.getContent()
                    + "\nb.getMember().getID() = " + b.getMember().getID()
                    + "\nb.getContent() = " + b.getContent()
                    + "\nb.getTitle() = " + b.getTitle()
                    + "\n만든날짜 = " + formattedCreateDate
                    + "\n수정날짜 = " + formattedModifyDate);
        }
    }
}
