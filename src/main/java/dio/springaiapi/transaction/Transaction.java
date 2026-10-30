package dio.springaiapi.transaction;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Transaction {

    public enum Type { INCOME, EXPENSE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String category;
    private LocalDate date;

    protected Transaction() {
    }

    public Transaction(String description, BigDecimal amount, Type type, String category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public Type getType() { return type; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
}
