package site.mylittlestore.repository.paymentmethod;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PaymentMethodRepositoryTest {
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("")
    void () {
        //given


        //when
        paymentMethodRepository.findNotPaidByIdAndPaymentId();

        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("paymentMethodId와 paymentId로 PAID인 paymentMethod를 payment와 함께 조회한다.")
    void findPaymentMetodWithPaymentByIdAndPaymentId() {
        //given


        //when
        paymentMethodRepository.findPaidWithPaymentByIdAndPaymentId();

        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("paymentId로 결제 수단을 모두 조회한다.")
    void findAllByPaymentId() {
        //given


        //when
        paymentMethodRepository.findAllByPaymentId();

        //then

        assertThat(1).isEqualTo(2);
    }

}