package dio.springaiapi.transaction;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    public record TransactionRequest(String description, BigDecimal amount, Transaction.Type type,
                                     String category, LocalDate date) {
    }

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@RequestBody TransactionRequest req) {
        return service.create(req.description(), req.amount(), req.type(), req.category(), req.date());
    }

    @GetMapping
    public List<Transaction> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.list(start, end);
    }

    @GetMapping("/balance")
    public Map<String, BigDecimal> balance() {
        return Map.of("balance", service.balance());
    }

    @GetMapping("/expenses-by-category")
    public Map<String, BigDecimal> byCategory(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.expensesByCategory(start, end);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}
