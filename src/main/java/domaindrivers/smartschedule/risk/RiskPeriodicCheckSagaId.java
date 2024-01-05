package domaindrivers.smartschedule.risk;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RiskPeriodicCheckSagaId implements Serializable {

    public static RiskPeriodicCheckSagaId newOne() {
        return new RiskPeriodicCheckSagaId(UUID.randomUUID());
    }

    private UUID projectRiskSagaId;

    RiskPeriodicCheckSagaId(UUID uuid) {
        this.projectRiskSagaId = uuid;
    }

    public RiskPeriodicCheckSagaId() {
    }

    public UUID id() {
        return projectRiskSagaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskPeriodicCheckSagaId projectId1 = (RiskPeriodicCheckSagaId) o;
        return Objects.equals(projectRiskSagaId, projectId1.projectRiskSagaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectRiskSagaId);
    }
}

