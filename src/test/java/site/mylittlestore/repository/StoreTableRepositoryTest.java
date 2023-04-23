package site.mylittlestore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.domain.Member;
import site.mylittlestore.domain.Store;
import site.mylittlestore.domain.StoreTable;
import site.mylittlestore.enumstorage.errormessage.StoreTableErrorMessage;
import site.mylittlestore.enumstorage.status.StoreTableStatus;
import site.mylittlestore.exception.storetable.NoSuchStoreTableException;
import site.mylittlestore.repository.member.MemberRepository;
import site.mylittlestore.repository.store.StoreRepository;
import site.mylittlestore.repository.storetable.StoreTableRepository;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StoreTableRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StoreTableRepository storeTableRepository;

    private Long storeTestId1;
    private Long storeTestId2;

    private Long storeTableTestId1_1;
    private Long storeTableTestId1_2;
    private Long storeTableTestId1_3;

    private Long storeTableTestId2_1;
    private Long storeTableTestId2_2;
    private Long storeTableTestId2_3;

    @BeforeEach
    void setUp() {
        //회원 생성
        Member member = memberRepository.save(Member.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게1 생성
        Store store1 = storeRepository.save(Store.builder()
                .member(member)
                .name("storeTest1")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게2 생성
        Store store2 = storeRepository.save(Store.builder()
                .member(member)
                .name("storeTest2")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //EMPTY, USING, DELETED 하나씩 넣어놓기
        //가게1
        //테이블1 생성
        StoreTable storeTable1_1 = storeTableRepository.save(StoreTable.builder()
                        .store(store1)
                    .build());

        //테이블2 생성
        StoreTable storeTable1_2 = storeTableRepository.save(StoreTable.builder()
                .store(store1)
                .build());
        //테이블2의 상태를 USING으로 변경
        storeTable1_2.changeStoreTableStatusUsing();
        //저장
        storeTableRepository.save(storeTable1_2);

        //테이블3 생성
        StoreTable storeTable1_3 = storeTableRepository.save(StoreTable.builder()
                .store(store1)
                .build());
        //테이블3의 상태를 DELETED로 변경
        storeTable1_3.delete();
        //저장
        storeTableRepository.save(storeTable1_3);



        //가게2
        //테이블1 생성
        StoreTable storeTable2_1 = storeTableRepository.save(StoreTable.builder()
                .store(store2)
                .build());

        //테이블2 생성
        StoreTable storeTable2_2 = storeTableRepository.save(StoreTable.builder()
                .store(store2)
                .build());
        //테이블2의 상태를 DELETED으로 변경
        storeTable2_2.delete();
        //저장
        storeTableRepository.save(storeTable2_2);

        //테이블3 생성
        StoreTable storeTable2_3 = storeTableRepository.save(StoreTable.builder()
                .store(store2)
                .build());
        //테이블3의 상태를 USING으로 변경
        storeTable2_3.changeStoreTableStatusUsing();
        //저장
        storeTableRepository.save(storeTable2_3);



        storeTestId1 = store1.getId();
        storeTestId2 = store2.getId();

        storeTableTestId1_1 = storeTable1_1.getId();
        storeTableTestId1_2 = storeTable1_2.getId();
        storeTableTestId1_3 = storeTable1_3.getId();

        storeTableTestId2_1 = storeTable2_1.getId();
        storeTableTestId2_2 = storeTable2_2.getId();
        storeTableTestId2_3 = storeTable2_3.getId();
    }

    @Test
    @DisplayName("storeTableId로 storeTableStatus가 DELETED가 아닌 storeTable을 찾는다.")
    void findNotDeletedById() {
        //when
        //테이블1을 찾는다.
        StoreTable storeTable1 = storeTableRepository.findNotDeletedById(storeTableTestId1_1)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));

        //테이블2를 찾는다.
        StoreTable storeTable2 = storeTableRepository.findNotDeletedById(storeTableTestId1_2)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));

        //then
        //테이블3을 찾는다.
        assertThatThrownBy(() -> storeTableRepository.findNotDeletedById(storeTableTestId1_3)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage())))
            .isInstanceOf(NoSuchStoreTableException.class);
    }

    @Test
    @DisplayName("storeTableId와 storeId로 StoreTableStatus가 DELETED가 아닌 storeTable을 찾는다.")
    void findNotDeletedByIdAndStoreId() {
        //when
        //가게1의 테이블2를 찾는다.(테이블2의 상태는 USING)
        StoreTable storeTable1_2 = storeTableRepository.findNotDeletedByIdAndStoreId(storeTableTestId1_2, storeTestId1)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));

        //가게2의 테이블1을 찾는다.(테이블1의 상태는 EMPTY)
        StoreTable storeTable2_1 = storeTableRepository.findNotDeletedByIdAndStoreId(storeTableTestId2_1, storeTestId2)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));

        //then
        //가게1의 테이블2의 상태가 USING이 맞는지 확인
        assertThat(storeTable1_2.getStoreTableStatus()).isEqualTo(StoreTableStatus.USING);

        //가게2의 테이블1의 상태가 EMPTY가 맞는지 확인
        assertThat(storeTable2_1.getStoreTableStatus()).isEqualTo(StoreTableStatus.EMPTY);

        //가게2의 Id와 가게1의 테이블1 Id로 찾는다.
        assertThatThrownBy(() -> storeTableRepository.findNotDeletedByIdAndStoreId(storeTableTestId1_1, storeTestId2)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage())))
            .isInstanceOf(NoSuchStoreTableException.class);
    }
}
