package site.mylittlestore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.repository.payment.PaymentRepository;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("paymentId와 orderId로 SUCCESS가 아닌 결제를 조회한다.")
    void findNotSuccessByIdAndOrderId() {
        //given


        //when
        paymentRepository.findNotSuccessByIdAndOrderId()

        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("paymentId와 orderId로 SUCCESS가 아닌 결제를 paymentMethods, order와 함께 조회한다.")
    void findNotSuccessWithPaymentMethodsAndOrderByIdAndOrderId() {
        //given


        //when
        paymentRepository.findNotSuccessWithPaymentMethodsAndOrderByIdAndOrderId()

        //then

        assertThat(1).isEqualTo(2);
    }
}