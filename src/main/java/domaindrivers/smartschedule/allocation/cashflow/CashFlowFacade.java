package domaindrivers.smartschedule.allocation.cashflow;

import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import domaindrivers.smartschedule.shared.EventsPublisher;

import java.time.Clock;


public class CashFlowFacade {

    private final CashflowRepository cashflowRepository;
    private final EventsPublisher eventsPublisher;
    private final Clock clock;

    public CashFlowFacade(CashflowRepository cashflowRepository, EventsPublisher eventsPublisher, Clock clock) {
        this.cashflowRepository = cashflowRepository;
        this.eventsPublisher = eventsPublisher;
        this.clock = clock;
    }

    public void addIncomeAndCost(ProjectAllocationsId projectId, Income income, Cost cost) {
        Cashflow cashflow = cashflowRepository.findById(projectId)
                .orElseGet(() -> new Cashflow(projectId));
        cashflow.update(income, cost);
        eventsPublisher.publish(new EarningsRecalculated(projectId, cashflow.earnings(), clock.instant()));
        cashflowRepository.save(cashflow);
    }

    public Earnings find(ProjectAllocationsId projectId) {
        Cashflow byId = cashflowRepository.findById(projectId).orElseThrow();
        return byId.earnings();
    }

}
