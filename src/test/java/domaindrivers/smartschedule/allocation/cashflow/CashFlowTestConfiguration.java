package domaindrivers.smartschedule.allocation.cashflow;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import domaindrivers.smartschedule.shared.EventsPublisher;

import java.time.Clock;
import java.util.*;

class CashFlowTestConfiguration {

    static CashFlowFacade cashFlowFacade(EventsPublisher eventsPublisher, Clock clock) {
        return new CashFlowFacade(new InMemoryCashflowRepository(), eventsPublisher, clock);
    }

}

class InMemoryCashflowRepository implements CashflowRepository {

    private final Map<ProjectAllocationsId, Cashflow> cashflows = new HashMap<>();

    @Override
    public Cashflow save(Cashflow cashflow) {
        return cashflows.put(cashflow.projectId, cashflow);
    }

    @Override
    public List<Cashflow> findAll() {
        return new ArrayList<>(cashflows.values());
    }

    @Override
    public Optional<Cashflow> findById(ProjectAllocationsId projectId) {
        return Optional.ofNullable(cashflows.get(projectId));
    }
}
