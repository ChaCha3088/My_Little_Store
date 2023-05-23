package site.mylittlestore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentMethodServiceTest {
    @Autowired
    private PaymentMethodService paymentMethodService;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("orderId와 paymentId로 PaymentMethodDto를 모두 조회한다.")
    void findAllPaymentMethodDtosByOrderIdAndPaymentId() {
        //given


        //when
//        paymentMethodService.findAllPaymentMethodDtosByOrderIdAndPaymentId();

        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("결제 수단을 생성한다.")
    void createPaymentMethod() {
        //given


        //when
//        paymentMethodService.createPaymentMethod()

        //then

        assertThat(1).isEqualTo(2);
    }
}