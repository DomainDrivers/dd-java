package domaindrivers.smartschedule.allocation.cashflow;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Earnings {

    public static Earnings of(int integer) {
        return new Earnings(BigDecimal.valueOf(integer));
    }

    private BigDecimal earnings;

    Earnings(BigDecimal value) {
        this.earnings = value;
    }

    public Earnings() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Earnings earnings1 = (Earnings) o;
        return Objects.equals(earnings, earnings1.earnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(earnings);
    }

    public BigDecimal toBigDecimal() {
        return earnings;
    }

    public boolean greaterThan(Earnings value) {
        return earnings.compareTo(value.toBigDecimal()) > 0;
    }
}
