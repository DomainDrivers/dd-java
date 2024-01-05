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
        riskSagaDispatcher.handle(new ProjectAllocationsDemandsScheduled(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));

        //when
        riskSagaDispatcher.handle(new CapabilitiesAllocated(UUID.randomUUID(), projectId, Demands.none(), Instant.now(clock)));

        //then
        Mockito.verify(riskPushNotification).notifyDemandsSatisfied(projectId);
    }

    @Test
    void informsAboutPotentialRiskWhenResourceTakenOver() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Capability java = Capability.skill("JAVA-MID-JUNIOR");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        //and
        riskSagaDispatcher.handle(new ProjectAllocationsDemandsScheduled(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new CapabilitiesAllocated(UUID.randomUUID(),projectId, Demands.none(), Instant.now(clock)));
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

    @Test
    void weeklyCheckDoesNothingWhenNotCloseToDeadlineAndDemandsNotSatisfied() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Capability java = Capability.skill("JAVA-MID-JUNIOR");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        //and
        riskSagaDispatcher.handle(new ProjectAllocationsDemandsScheduled(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new ProjectAllocationScheduled(projectId, PROJECT_DATES, Instant.now(clock)));

        //when
        itIsDaysBeforeDeadline(100);
        riskSagaDispatcher.handleWeeklyCheck();

        //then
        Mockito.verifyNoMoreInteractions(riskPushNotification);
    }

    @Test
    void weeklyCheckDoesNothingWhenCloseToDeadlineAndDemandsSatisfied() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Capability java = Capability.skill("JAVA-MID-JUNIOR-UNIQUE");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        riskSagaDispatcher.handle(new ProjectAllocationsDemandsScheduled(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new EarningsRecalculated(projectId, Earnings.of(10), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new CapabilitiesAllocated(UUID.randomUUID(),projectId, Demands.none(), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new ProjectAllocationScheduled(projectId, PROJECT_DATES, Instant.now(clock)));

        //when
        itIsDaysBeforeDeadline(100);
        Mockito.reset(riskPushNotification);
        riskSagaDispatcher.handleWeeklyCheck();

        //then
        Mockito.verifyNoMoreInteractions(riskPushNotification);
    }

    @Test
    void findReplacementsWhenDeadlineClose() {
        //given
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        Capability java = Capability.skill("JAVA-MID-JUNIOR");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        riskSagaDispatcher.handle(new ProjectAllocationsDemandsScheduled(projectId, Demands.of(javaOneDayDemand), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new EarningsRecalculated(projectId, Earnings.of(10), Instant.now(clock)));
        //and
        riskSagaDispatcher.handle(new ProjectAllocationScheduled(projectId, PROJECT_DATES, Instant.now(clock)));
        //and
        AllocatableCapabilityId employee = thereIsEmployeeWithSkills(Set.of(java), ONE_DAY_LONG);

        //when
        Mockito.reset(riskPushNotification);
        itIsDaysBeforeDeadline(20);
        riskSagaDispatcher.handleWeeklyCheck();

        //then
        Mockito.verify(riskPushNotification).notifyAboutAvailability(eq(projectId), argThat(employeeWasSuggestedForDemand(javaOneDayDemand, employee)));
    }

    @Test
    void suggestResourcesFromDifferentProjects() {
        //given
        ProjectAllocationsId highValueProject = ProjectAllocationsId.newOne();
        ProjectAllocationsId lowValueProject = ProjectAllocationsId.newOne();
        //and
        Capability java = Capability.skill("JAVA-MID-JUNIOR-SUPER-UNIQUE");
        Demand javaOneDayDemand = new Demand(java, ONE_DAY_LONG);
        //and
        allocationFacade.scheduleProjectAllocationDemands(highValueProject, Demands.of(javaOneDayDemand));
        cashFlowFacade.addIncomeAndCost(highValueProject, Income.of(10000), Cost.of(10));
        allocationFacade.scheduleProjectAllocationDemands(lowValueProject, Demands.of(javaOneDayDemand));
        cashFlowFacade.addIncomeAndCost(lowValueProject, Income.of(100), Cost.of(10));
        //and
        AllocatableCapabilityId employee = thereIsEmployeeWithSkills(Set.of(java), ONE_DAY_LONG);
        allocationFacade.allocateToProject(lowValueProject, employee, ONE_DAY_LONG);
        //and
        riskSagaDispatcher.handle(new ProjectAllocationScheduled(highValueProject, PROJECT_DATES, Instant.now(clock)));

        //when
        Mockito.reset(riskPushNotification);
        allocationFacade.editProjectDates(highValueProject, PROJECT_DATES);
        allocationFacade.editProjectDates(lowValueProject, PROJECT_DATES);
        itIsDaysBeforeDeadline(1);
        riskSagaDispatcher.handleWeeklyCheck();

        //then
        Mockito.verify(riskPushNotification, timeout(1000)).notifyProfitableRelocationFound(highValueProject, employee);
    }

    ArgumentMatcher<Map<Demand, AllocatableCapabilitiesSummary>> employeeWasSuggestedForDemand(Demand demand, AllocatableCapabilityId allocatableCapabilityId) {
        return suggestions -> suggestions.get(demand).all().stream().anyMatch(suggestion -> suggestion.id().equals(allocatableCapabilityId));
    }

    AllocatableCapabilityId thereIsEmployeeWithSkills(Set<Capability> skills, TimeSlot inSlot) {
        EmployeeId staszek = employeeFacade.addEmployee("Staszek", "Staszkowski", Seniority.MID, skills, Capability.permissions());
        List<AllocatableCapabilityId> allocatableCapabilityIds = employeeFacade.scheduleCapabilities(staszek, inSlot);
        assert allocatableCapabilityIds.size() == 1;
        return allocatableCapabilityIds.get(0);
    }

    void itIsDaysBeforeDeadline(int days) {
        Mockito.when(clock.instant()).thenReturn(PROJECT_DATES.to().minus(days, ChronoUnit.DAYS));
    }
}

