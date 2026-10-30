package dio.springaiapi.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TransactionService.class)
class TransactionServiceTest {

    @Autowired
    TransactionService service;

    @Test
    void savesValidTransactionWithDefaults() {
        Transaction t = service.create("Almoço", new BigDecimal("35.50"), Transaction.Type.EXPENSE, null, null);
        assertNotNull(t.getId());
        assertEquals("outros", t.getCategory());
        assertEquals(LocalDate.now(), t.getDate());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create("x", BigDecimal.ZERO, Transaction.Type.EXPENSE, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(" ", BigDecimal.TEN, Transaction.Type.EXPENSE, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.create("x", BigDecimal.TEN, Transaction.Type.EXPENSE, null, LocalDate.now().plusDays(1)));
    }

    @Test
    void computesBalanceAndExpensesByCategory() {
        service.create("Salário", new BigDecimal("1000"), Transaction.Type.INCOME, "salário", null);
        service.create("Mercado", new BigDecimal("200"), Transaction.Type.EXPENSE, "Alimentação", null);
        service.create("Lanche", new BigDecimal("50"), Transaction.Type.EXPENSE, "alimentação", null);

        assertEquals(0, new BigDecimal("750").compareTo(service.balance()));
        assertEquals(0, new BigDecimal("250").compareTo(service.expensesByCategory(null, null).get("alimentação")));
    }
}
