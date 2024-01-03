package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.planning.schedule.assertions.ScheduleAssert;
import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

import static domaindrivers.smartschedule.planning.Demand.demandFor;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static java.time.Duration.ofDays;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-planning.sql"})
@Ignore
class VisionTest {

    static final Instant JAN_1 = Instant.parse("2020-01-01T00:00:00.00Z");
    static final TimeSlot JAN_1_2 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-02T00:00:00.00Z"));
    static final TimeSlot JAN_2_5 = new TimeSlot(Instant.parse("2020-01-02T00:00:00.00Z"), Instant.parse("2020-01-05T00:00:00.00Z"));
    static final TimeSlot JAN_2_12 = new TimeSlot(Instant.parse("2020-01-02T00:00:00.00Z"), Instant.parse("2020-01-12T00:00:00.00Z"));
    static final ResourceName RESOURCE_1 = new ResourceName("r1");
    static final ResourceName RESOURCE_2 = new ResourceName("r2");
    static final ResourceName RESOURCE_4 = new ResourceName("r4");

    @Autowired
    PlanningFacade projectFacade;

    @Test
    @Disabled("not implemented yet")
    void visionValidationProcess() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("vision");
        //when
        Demands java = Demands.of(demandFor(skill("JAVA")));
        projectFacade.addDemands(projectId, java);

        //then
        verifyPossibleRiskDuringPlanning(projectId, java);

        //when
        projectFacade.defineProjectStages(projectId,
                new Stage("stage1")
                        .withChosenResourceCapabilities(RESOURCE_1),
                new Stage("stage2")
                        .withChosenResourceCapabilities(RESOURCE_2, RESOURCE_1),
                new Stage("stage3")
                        .withChosenResourceCapabilities(RESOURCE_4));

        //then
        ProjectCard projectCard = projectFacade.load(projectId);
        assertThat(projectCard.parallelizedStages().print())
                .isIn("stage1 | stage2, stage3",
                              "stage2, stage3 | stage1");

        //when
        projectFacade.defineProjectStages(projectId,
                new Stage("stage1")
                        .ofDuration(ofDays(1))
                        .withChosenResourceCapabilities(RESOURCE_1),
                new Stage("stage2")
                        .ofDuration(ofDays(3))
                        .withChosenResourceCapabilities(RESOURCE_2, RESOURCE_1),
                new Stage("stage3")
                        .ofDuration(ofDays(10))
                        .withChosenResourceCapabilities(RESOURCE_4));
        //and
        projectFacade.defineStartDate(projectId, JAN_1);

        //then
        Schedule schedule = projectFacade.load(projectId).schedule();
        ScheduleAssert.assertThat(schedule)
                .hasStage("stage1").withSlot(JAN_1_2)
                .and()
                .hasStage("stage2").withSlot(JAN_2_5)
                .and()
                .hasStage("stage3").withSlot(JAN_2_12);

    }

    void verifyPossibleRiskDuringPlanning(ProjectId projectId, Demands demands) {
    }

}