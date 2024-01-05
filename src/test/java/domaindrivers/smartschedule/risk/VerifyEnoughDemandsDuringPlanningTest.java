package domaindrivers.smartschedule.risk;

import domaindrivers.smartschedule.TaskExecutorConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.planning.Demand;
import domaindrivers.smartschedule.planning.Demands;
import domaindrivers.smartschedule.planning.PlanningFacade;
import domaindrivers.smartschedule.planning.ProjectId;
import domaindrivers.smartschedule.resource.employee.EmployeeFacade;
import domaindrivers.smartschedule.shared.capability.Capability;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static domaindrivers.smartschedule.resource.employee.Seniority.SENIOR;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static org.mockito.Mockito.*;


@SpringBootTest
@Import({TestDbConfiguration.class, TaskExecutorConfiguration.class})
@Sql(scripts = {"classpath:schema-availability.sql", "classpath:schema-resources.sql", "classpath:schema-risk.sql"})
class VerifyEnoughDemandsDuringPlanningTest {

    @MockBean
    RiskPushNotification riskPushNotification;

    @Autowired
    VerifyEnoughDemandsDuringPlanning verifyEnoughDemandsDuringPlanning;

    @Autowired
    EmployeeFacade employeeFacade;

    @Autowired
    PlanningFacade planningFacade;

    @Test
    void doesNothingWhenEnoughResources() {
        //given
        employeeFacade.addEmployee("resourceName", "lastName", SENIOR, Capability.skills("JAVA5", "PYTHON"), Capability.permissions());
        employeeFacade.addEmployee("resourceName", "lastName", SENIOR, Capability.skills("C#", "RUST"), Capability.permissions());
        //and
        ProjectId projectId = planningFacade.addNewProject("java5");

        //when
        planningFacade.addDemands(projectId, Demands.of(new Demand(skill("JAVA5"))));

        // then
        verify(riskPushNotification, after(1000).never()).notifyAboutPossibleRiskDuringPlanning(projectId, Demands.of(Demand.demandFor(skill("JAVA"))));
    }

    @Test
    void notifiesWhenNotEnoughResources() {
        //given
        employeeFacade.addEmployee("resourceName", "lastName", SENIOR, Capability.skills("JAVA"), Capability.permissions());
        employeeFacade.addEmployee("resourceName", "lastName", SENIOR, Capability.skills("C"), Capability.permissions());
        //and
        ProjectId java = planningFacade.addNewProject("java");
        ProjectId c = planningFacade.addNewProject("C");
        //and
        planningFacade.addDemands(java, Demands.of(new Demand(skill("JAVA"))));
        planningFacade.addDemands(c, Demands.of(new Demand(skill("C"))));
        //when
        ProjectId rust = planningFacade.addNewProject("rust");
        planningFacade.addDemands(rust, Demands.of(new Demand(skill("RUST"))));

        //then
        verify(riskPushNotification, timeout(1000)).notifyAboutPossibleRiskDuringPlanning(rust, Demands.of(Demand.demandFor(skill("RUST"))));
    }

}