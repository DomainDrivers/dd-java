package domaindrivers.smartschedule.allocation.cashflow;

import domaindrivers.smartschedule.allocation.ProjectAllocationsId;



public class CashFlowFacade {

    private final CashflowRepository cashflowRepository;

    public CashFlowFacade(CashflowRepository cashflowRepository) {
        this.cashflowRepository = cashflowRepository;
    }

    public void addIncomeAndCost(ProjectAllocationsId projectId, Income income, Cost cost) {
        Cashflow cashflow = cashflowRepository.findById(projectId)
                .orElseGet(() -> new Cashflow(projectId));
        cashflow.update(income, cost);
        cashflowRepository.save(cashflow);
    }

    public Earnings find(ProjectAllocationsId projectId) {
        Cashflow byId = cashflowRepository.findById(projectId).orElseThrow();
        return byId.earnings();
    }

}
