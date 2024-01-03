package domaindrivers.smartschedule.allocation.cashflow;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity(name = "cashflows")
class Cashflow {

    @EmbeddedId
    ProjectAllocationsId projectId;

    @Embedded
    Income income;

    @Embedded
    Cost cost;

    public Cashflow() {
    }

    Cashflow(ProjectAllocationsId projectId) {
        this.projectId = projectId;
    }

    Earnings earnings() {
        return income.minus(cost);
    }

    public void update(Income income, Cost cost) {
        this.income = income;
        this.cost = cost;
    }
}
