package domaindrivers.smartschedule.allocation.cashflow;

import org.junit.jupiter.api.Test;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;

class EarningsTest {
    
    @Test
    void incomeMinusCostTest() {
        //expect
        assertEquals(Earnings.of(9), Income.of(TEN).minus(Cost.of(1)));
        assertEquals(Earnings.of(8), Income.of(TEN).minus(Cost.of(2)));
        assertEquals(Earnings.of(7), Income.of(TEN).minus(Cost.of(3)));
        assertEquals(Earnings.of(-70), Income.of(100).minus(Cost.of(170)));
    }

    @Test
    void greaterThanTest() {
        assertTrue(Earnings.of(10).greaterThan(Earnings.of(9)));
        assertTrue(Earnings.of(10).greaterThan(Earnings.of(0)));
        assertTrue(Earnings.of(10).greaterThan(Earnings.of(-1)));
        assertFalse(Earnings.of(10).greaterThan(Earnings.of(10)));
        assertFalse(Earnings.of(10).greaterThan(Earnings.of(11)));
    }

}