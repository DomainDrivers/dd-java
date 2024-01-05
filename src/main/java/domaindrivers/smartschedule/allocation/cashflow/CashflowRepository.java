package domaindrivers.smartschedule.allocation.cashflow;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;

import java.util.List;
import java.util.Optional;


interface CashflowRepository  {

    Optional<Cashflow> findById(ProjectAllocationsId projectId);

    Cashflow save(Cashflow cashflow);

    List<Cashflow> findAll();
}