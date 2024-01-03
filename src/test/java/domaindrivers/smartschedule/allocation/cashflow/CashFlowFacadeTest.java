package domaindrivers.smartschedule.allocation.cashflow;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = "classpath:schema-cashflow.sql")
class CashFlowFacadeTest {

    @Autowired
    CashFlowFacade cashFlowFacade;

    @Test
    void canSaveCashFlow() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();

        //when
        cashFlowFacade.addIncomeAndCost(projectId, Income.of(100), Cost.of(50));

        //then
        assertEquals(Earnings.of(50), cashFlowFacade.find(projectId));
    }

}