package domaindrivers.smartschedule.allocation.cashflow;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Cost {

    public static Cost of(int integer) {
        return new Cost(BigDecimal.valueOf(integer));
    }

    private BigDecimal cost;

    Cost(BigDecimal value) {
        this.cost = value;
    }

    public Cost() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cost cost1 = (Cost) o;
        return Objects.equals(cost, cost1.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost);
    }

    BigDecimal value() {
        return cost;
    }
}
