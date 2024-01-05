package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.MockedClockConfiguration;
import domaindrivers.smartschedule.TaskExecutorConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.allocation.*;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitiesSummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.cashflow.*;
import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.availability.ResourceTakenOver;
import domaindrivers.smartschedule.planning.ProjectId;
import domaindrivers.smartschedule.resource.employee.EmployeeFacade;
import domaindrivers.smartschedule.resource.employee.EmployeeId;
import domaindrivers.smartschedule.resource.employee.Seniority;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;

@SpringBootTest
@Import({TestDbConfiguration.class, MockedClockConfiguration.class, TaskExecutorConfiguration.class})
@Sql(scripts = {"classpath:schema-planning.sql", "classpath:schema-availability.sql", "classpath:schema-resources.sql", "classpath:schema-allocations.sql", "classpath:schema-risk.sql", "classpath:schema-cashflow.sql"})
class RiskPeriodicCheckSagaDispatcherE2ETest {

    static final TimeSlot ONE_DAY_LONG = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
    static final TimeSlot PROJECT_DATES =
            new TimeSlot(Instant.now(), Instant.now().plus(20, ChronoUnit.DAYS));

    @Autowired
    EmployeeFacade employeeFacade;

    @Autowired
    AllocationFacade allocationFacade;

    @Autowired
    RiskPeriodicCheckSagaDispatcher riskSagaDispatcher;

    @MockBean
    RiskPushNotification riskPushNotification;

    @Autowired
    CashFlowFacade cashFlowFacade;

    @Autowired
    Clock clock;

    @Test
    void informsAboutDemandSatisfied() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Capability java = Capability.skill("JAVA-MID-JUNIOR");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        //and
        riskSagaDispatcher.handle(NotSatisfiedDemands.forOneProject(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));

        //when
        riskSagaDispatcher.handle(NotSatisfiedDemands.allSatisfied(projectId, Instant.now(clock)));

        //then
        Mockito.verify(riskPushNotification).notifyDemandsSatisfied(projectId);
    }

    @Test
    void informsAboutDemandSatisfiedForAllProjects() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        ProjectAllocationsId projectId2 = ProjectAllocationsId.newOne();
        //and
        Map<ProjectAllocationsId, Demands> noMissingDemands =
                Map.of(projectId, Demands.none(),
                        projectId2, Demands.none());
        //when
        riskSagaDispatcher.handle(new NotSatisfiedDemands(noMissingDemands, Instant.now(clock)));

        //then
        Mockito.verify(riskPushNotification).notifyDemandsSatisfied(projectId);
        Mockito.verify(riskPushNotification).notifyDemandsSatisfied(projectId2);
    }

    @Test
    void informsAboutPotentialRiskWhenResourceTakenOver() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Capability java = Capability.skill("JAVA-MID-JUNIOR");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        //and
        riskSagaDispatcher.handle(NotSatisfiedDemands.forOneProject(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(NotSatisfiedDemands.allSatisfied(projectId, Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new ProjectAllocationScheduled(projectId, PROJECT_DATES, Instant.now(clock)));

        //when
        Mockito.reset(riskPushNotification);
        itIsDaysBeforeDeadline(100);
        riskSagaDispatcher.handle(new ResourceTakenOver(ResourceId.newOne(), Set.of(Owner.of(projectId.id())), ONE_DAY_LONG, Instant.now(clock)));

        //then
        Mockito.verify(riskPushNotification).notifyAboutPossibleRisk(projectId);
    }

    @Test
    void doesNothingWhenResourceTakenOverFromFromUnknownProject() {
        //given
        ProjectId unknown = ProjectId.newOne();
        //when
        riskSagaDispatcher.handle(new ResourceTakenOver(ResourceId.newOne(), Set.of(Owner.of(unknown.id())), ONE_DAY_LONG, Instant.now(clock)));

        //then
        Mockito.verifyNoInteractions(riskPushNotification);
    }

    ArgumentMatcher<Map<Demand, AllocatableCapabilitiesSummary>> employeeWasSuggestedForDemand(Demand demand, EmployeeId employee) {
        return suggestions -> suggestions.get(demand).all().stream().anyMatch(suggestion -> suggestion.allocatableResourceId().equals(employee.toAllocatableResourceId()));
    }

    void itIsDaysBeforeDeadline(int days) {
        Mockito.when(clock.instant()).thenReturn(PROJECT_DATES.to().minus(days, ChronoUnit.DAYS));
    }
}

