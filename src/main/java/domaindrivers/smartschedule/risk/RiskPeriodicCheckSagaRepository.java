package domaindrivers.smartschedule.risk;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


interface RiskPeriodicCheckSagaRepository extends JpaRepository<RiskPeriodicCheckSaga, RiskPeriodicCheckSagaId> {

    RiskPeriodicCheckSaga findByProjectId(ProjectAllocationsId projectId);

    List<RiskPeriodicCheckSaga> findByProjectIdIn(List<ProjectAllocationsId> interested);
}