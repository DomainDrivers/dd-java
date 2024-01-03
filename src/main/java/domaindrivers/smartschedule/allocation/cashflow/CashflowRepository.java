package domaindrivers.smartschedule.allocation.cashflow;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import org.springframework.data.jpa.repository.JpaRepository;


interface CashflowRepository extends JpaRepository<Cashflow, ProjectAllocationsId> {


}