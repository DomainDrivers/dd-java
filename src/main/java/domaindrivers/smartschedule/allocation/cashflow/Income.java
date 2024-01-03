package domaindrivers.smartschedule.allocation.cashflow;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Income {
    static Income of(BigDecimal bigDecimal) {
        return new Income(bigDecimal);
    }

    public static Income of(int integer) {
        return new Income(BigDecimal.valueOf(integer));
    }

    private BigDecimal income;

    Income(BigDecimal value) {
        this.income = value;
    }

    public Income() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Income earnings1 = (Income) o;
        return Objects.equals(income, earnings1.income);
    }

    @Override
    public int hashCode() {
        return Objects.hash(income);
    }

    public Earnings minus(Cost estimatedCosts) {
        return new Earnings(income.subtract(estimatedCosts.value()));
    }

}
