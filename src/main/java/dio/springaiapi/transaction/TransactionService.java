package dio.springaiapi.transaction;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class TransactionService {

    static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /** Melhoria: valida os dados antes de salvar (vale tanto para a IA quanto para o REST). */
    public Transaction create(String description, BigDecimal amount, Transaction.Type type, String category, LocalDate date) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descrição é obrigatória.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("O valor excede o limite de " + MAX_AMOUNT + ".");
        }
        if (type == null) {
            throw new IllegalArgumentException("Informe o tipo: INCOME (receita) ou EXPENSE (despesa).");
        }
        LocalDate when = date != null ? date : LocalDate.now();
        if (when.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data não pode estar no futuro.");
        }
        String cat = (category == null || category.isBlank()) ? "outros" : category.trim().toLowerCase();
        return repository.save(new Transaction(description.trim(), amount, type, cat, when));
    }

    public List<Transaction> list(LocalDate start, LocalDate end) {
        return repository.findByDateBetweenOrderByDateDesc(
                start != null ? start : LocalDate.of(1900, 1, 1),
                end != null ? end : LocalDate.now());
    }

    public BigDecimal balance() {
        return repository.findAll().stream()
                .map(t -> t.getType() == Transaction.Type.INCOME ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Melhoria: nova consulta — total de despesas por categoria no período. */
    public Map<String, BigDecimal> expensesByCategory(LocalDate start, LocalDate end) {
        Map<String, BigDecimal> result = new TreeMap<>();
        list(start, end).stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .forEach(t -> result.merge(t.getCategory(), t.getAmount(), BigDecimal::add));
        return result;
    }
}
