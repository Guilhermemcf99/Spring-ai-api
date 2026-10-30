package dio.springaiapi.assistant;

import dio.springaiapi.transaction.Transaction;
import dio.springaiapi.transaction.TransactionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Funções reais da aplicação que a IA pode chamar (Tool Calling). */
@Component
public class BudgetTools {

    private final TransactionService service;

    public BudgetTools(TransactionService service) {
        this.service = service;
    }

    @Tool(description = "Registra uma nova transação financeira (receita ou despesa).")
    public String createTransaction(
            @ToolParam(description = "Descrição curta, ex: 'almoço'") String description,
            @ToolParam(description = "Valor positivo em reais") BigDecimal amount,
            @ToolParam(description = "INCOME para receita, EXPENSE para despesa") Transaction.Type type,
            @ToolParam(description = "Categoria, ex: alimentação, transporte, salário", required = false) String category,
            @ToolParam(description = "Data no formato yyyy-MM-dd; vazio para hoje", required = false) LocalDate date) {
        try {
            Transaction t = service.create(description, amount, type, category, date);
            return "Transação #" + t.getId() + " salva com sucesso.";
        } catch (IllegalArgumentException e) {
            // devolve o erro para a IA explicar ao usuário em vez de quebrar o fluxo
            return "Não foi possível salvar: " + e.getMessage();
        }
    }

    @Tool(description = "Lista transações em um período. Datas no formato yyyy-MM-dd.")
    public List<Transaction> listTransactions(
            @ToolParam(description = "Data inicial", required = false) LocalDate start,
            @ToolParam(description = "Data final", required = false) LocalDate end) {
        return service.list(start, end);
    }

    @Tool(description = "Retorna o saldo atual (receitas menos despesas).")
    public BigDecimal getBalance() {
        return service.balance();
    }

    @Tool(description = "Retorna o total de despesas agrupado por categoria em um período.")
    public Map<String, BigDecimal> expensesByCategory(
            @ToolParam(description = "Data inicial", required = false) LocalDate start,
            @ToolParam(description = "Data final", required = false) LocalDate end) {
        return service.expensesByCategory(start, end);
    }
}
