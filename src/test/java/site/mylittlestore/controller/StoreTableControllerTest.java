package site.mylittlestore.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.storetable.StoreTableFindDto;
import site.mylittlestore.service.MemberService;
import site.mylittlestore.service.StoreService;
import site.mylittlestore.service.StoreTableService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StoreTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreTableService storeTableService;

    private Long memberTestId;

    private Long storeTestId;

    @BeforeAll
    void setUp() {
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                                        .city("city")
                        .street("street")
                        .zipcode("zipcode")
                .build());

        Long newStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(newMemberId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        memberTestId = newMemberId;
        storeTestId = newStoreId;
    }

    @Test
    @DisplayName("테이블 리스트")
    void storeTableList() {
        //given


        //when


        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("주문이 이미 있을 때 테이블 상세")
    void storeTableInfoWithOrder() throws Exception {
        //when
        //테이블 생성
        String redirectedUrl1 = mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/new", memberTestId, storeTestId))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        Long storeTableId = getId(redirectedUrl1);

        //주문 생성
        String redirectedUrl2 = mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/new", memberTestId, storeTestId, storeTableId))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        //접근시 주문 정보로 redirect 되는지 확인
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}", memberTestId, storeTestId, storeTableId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/" + memberTestId + "/stores/" + storeTestId + "/storeTables/" + storeTableId + "/orders/" + getId(redirectedUrl2)));
    }


    @Test
    @DisplayName("주문이 이미 없을 때 테이블 상세")
    void storeTableInfoWithoutOrder() throws Exception {
        //when
        //테이블 생성
        String redirectedUrl1 = mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/new", memberTestId, storeTestId))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        Long storeTableId = getId(redirectedUrl1);

        //then
        //storeTableFindDto가 있는지 확인
        //view가 storeTables/StoreTableInfo인지 확인
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}", memberTestId, storeTestId, storeTableId))
                .andExpect(status().isOk())
                .andExpect(view().name("storeTables/StoreTableInfo"))
                .andExpect(model().attributeExists("storeTableFindDto"));
    }

    @Test
    @DisplayName("테이블 생성")
    void createStoreTable() throws Exception {
        String redirectedUrl = mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/new", memberTestId, storeTestId))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        Long storeTableId = getId(redirectedUrl);

        StoreTableFindDto storeTableFindDtoById = storeTableService.findStoreTableFindDtoById(storeTableId);
        assertThat(storeTableFindDtoById.getStoreId()).isEqualTo(storeTestId);
        assertThat(storeTableFindDtoById.getStoreTableStatus()).isEqualTo("EMPTY");
    }

    private static Long getId(String redirectedUrl1) {
        String[] split1 = redirectedUrl1.split("/");
        Long storeTableId = Long.parseLong(split1[split1.length - 1]);
        return storeTableId;
    }
}
