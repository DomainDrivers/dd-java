package domaindrivers.smartschedule.allocation.cashflow;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;

@SpringBootTest
@Import({TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = "classpath:schema-cashflow.sql")
class CashFlowFacadeTest {

    @Autowired
    CashFlowFacade cashFlowFacade;

    @Autowired
    EventsPublisher eventsPublisher;

    @Test
    void canSaveCashFlow() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();

        //when
        cashFlowFacade.addIncomeAndCost(projectId, Income.of(100), Cost.of(50));

        //then
        assertEquals(Earnings.of(50), cashFlowFacade.find(projectId));
    }

    @Test
    void updatingCashFlowEmitsAnEvent() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Income income = Income.of(100);
        Cost cost = Cost.of(50);

        //when
        cashFlowFacade.addIncomeAndCost(projectId, income, cost);

        //then
        Mockito.verify(eventsPublisher).publish(argThat(isEarningsRecalculatedEvent(projectId, Earnings.of(50))));
    }

    ArgumentMatcher<EarningsRecalculated> isEarningsRecalculatedEvent(ProjectAllocationsId projectId, Earnings earnings) {
        return event -> event.projectId().equals(projectId) &&
                event.earnings().equals(earnings) &&
                event.occurredAt() != null;
    }


}