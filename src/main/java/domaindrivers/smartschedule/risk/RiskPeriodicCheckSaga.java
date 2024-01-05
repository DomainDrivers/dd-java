package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.allocation.*;
import domaindrivers.smartschedule.allocation.cashflow.Earnings;
import domaindrivers.smartschedule.allocation.cashflow.EarningsRecalculated;
import domaindrivers.smartschedule.availability.ResourceTakenOver;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Duration;
import java.time.Instant;


@Entity(name = "project_risk_sagas")
class RiskPeriodicCheckSaga {

    static final Earnings RISK_THRESHOLD_VALUE = Earnings.of(1000);
    static final int UPCOMING_DEADLINE_AVAILABILITY_SEARCH = 30;
    static final int UPCOMING_DEADLINE_REPLACEMENT_SUGGESTION = 15;

    @EmbeddedId
    private RiskPeriodicCheckSagaId riskSagaId;

    @Embedded
    private ProjectAllocationsId projectId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Demands missingDemands;

    @Embedded
    private Earnings earnings;

    @Version
    private int version;

    private Instant deadline;

    RiskPeriodicCheckSaga(ProjectAllocationsId projectId, Demands missingDemands) {
        this.riskSagaId = RiskPeriodicCheckSagaId.newOne();
        this.projectId = projectId;
        this.missingDemands = missingDemands;
    }

    RiskPeriodicCheckSaga(ProjectAllocationsId projectId, Earnings earnings) {
        this.riskSagaId = RiskPeriodicCheckSagaId.newOne();
        this.projectId = projectId;
        this.missingDemands = Demands.none();
        this.earnings = earnings;
    }

    public RiskPeriodicCheckSaga() {
    }

    boolean areDemandsSatisfied() {
        return false;
    }

    Demands missingDemands() {
        return missingDemands;
    }

    RiskPeriodicCheckSagaStep handle(EarningsRecalculated event) {
        return null;
    }

    RiskPeriodicCheckSagaStep handle(ProjectAllocationsDemandsScheduled event) {
        return null;
    }

    RiskPeriodicCheckSagaStep handle(ProjectAllocationScheduled event) {
        return null;
    }

    RiskPeriodicCheckSagaStep handle(ResourceTakenOver event) {
        return null;
    }

    RiskPeriodicCheckSagaStep handle(CapabilityReleased event) {
        return null;
    }

    RiskPeriodicCheckSagaStep handle(CapabilitiesAllocated event) {
        return null;
    }

    RiskPeriodicCheckSagaStep handleWeeklyCheck(Instant when) {
        return null;
    }

    ProjectAllocationsId projectId() {
        return projectId;
    }

    Earnings earnings() {
        return earnings;
    }

    Instant deadline() {
        return deadline;
    }

}
