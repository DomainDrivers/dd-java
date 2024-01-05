package domaindrivers.smartschedule.risk;


import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


interface RiskPeriodicCheckSagaRepository extends JpaRepository<RiskPeriodicCheckSaga, RiskPeriodicCheckSagaId> {

    RiskPeriodicCheckSaga findByProjectId(ProjectAllocationsId projectId);

    List<RiskPeriodicCheckSaga> findByProjectIdIn(List<ProjectAllocationsId> interested);

    default RiskPeriodicCheckSaga findByProjectIdOrCreate(ProjectAllocationsId projectId) {
        RiskPeriodicCheckSaga found = findByProjectId(projectId);
        if (found == null) {
            found = save(new RiskPeriodicCheckSaga(projectId));
        }
        return found;
    }

    default List<RiskPeriodicCheckSaga> findByProjectIdInOrElseCreate(List<ProjectAllocationsId> interested) {
        List<RiskPeriodicCheckSaga> found = findByProjectIdIn(interested);
        List<ProjectAllocationsId> foundIds = found
                .stream()
                .map(RiskPeriodicCheckSaga::projectId).
                toList();
        List<RiskPeriodicCheckSaga> missing =
                interested
                        .stream()
                        .filter(projectId -> !foundIds.contains(projectId))
                        .map(RiskPeriodicCheckSaga::new).toList();
        missing = saveAll(missing);
        found.addAll(missing);
        return found;
    }
}