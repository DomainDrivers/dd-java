package domaindrivers.smartschedule.allocation.cashflow;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import org.springframework.data.jpa.repository.JpaRepository;


interface JpaCashflowRepository extends CashflowRepository, JpaRepository<Cashflow, ProjectAllocationsId> {


}