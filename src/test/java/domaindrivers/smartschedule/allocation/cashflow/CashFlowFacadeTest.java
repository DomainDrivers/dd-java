package domaindrivers.smartschedule.allocation.cashflow;

import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import domaindrivers.smartschedule.shared.EventsPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;


class CashFlowFacadeTest {

    static final Instant NOW = Instant.now();

    EventsPublisher eventsPublisher = Mockito.mock(EventsPublisher.class);
    Clock clock = Clock.fixed(NOW, ZoneId.systemDefault());
    CashFlowFacade cashFlowFacade = CashFlowTestConfiguration.cashFlowFacade(eventsPublisher, clock);

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