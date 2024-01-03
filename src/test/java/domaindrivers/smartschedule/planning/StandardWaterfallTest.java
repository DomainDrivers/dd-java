package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.Map;

import static domaindrivers.smartschedule.planning.Demand.demandFor;
import static domaindrivers.smartschedule.planning.schedule.assertions.ScheduleAssert.assertThat;
import static domaindrivers.smartschedule.shared.capability.Capability.skill;
import static java.time.Duration.ofDays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-planning.sql"})
class StandardWaterfallTest {

    static final Instant JAN_1 = Instant.parse("2020-01-01T00:00:00.00Z");
    static final ResourceId RESOURCE_1 = ResourceId.newOne();
    static final ResourceId RESOURCE_2 = ResourceId.newOne();
    static final ResourceId RESOURCE_4 = ResourceId.newOne();

    static final TimeSlot JAN_1_2 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-02T00:00:00.00Z"));
    static final TimeSlot JAN_2_5 = new TimeSlot(Instant.parse("2020-01-02T00:00:00.00Z"), Instant.parse("2020-01-05T00:00:00.00Z"));
    static final TimeSlot JAN_2_12 = new TimeSlot(Instant.parse("2020-01-02T00:00:00.00Z"), Instant.parse("2020-01-12T00:00:00.00Z"));

    @Autowired
    PlanningFacade projectFacade;

    @Test
    @Disabled("not implemented yet")
    void waterfallProjectProcess() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("waterfall");

        //when
        projectFacade.defineProjectStages(projectId,
                new Stage("stage1"),
                new Stage("stage2"),
                new Stage("stage3")
        );

        //then
        ProjectCard projectCard = projectFacade.load(projectId);
        assertEquals("stage1, stage2, stage3", projectCard.parallelizedStages().print());

        //when
        DemandsPerStage demandsPerStage = new DemandsPerStage(Map.of("stage1", Demands.of(demandFor(skill("java")))));
        projectFacade.defineDemandsPerStage(projectId, demandsPerStage);

        //then
        verifyRiskDuringPlanning(projectId);

        //when
        projectFacade.defineProjectStages(projectId,
                new Stage("stage1")
                        .withChosenResourceCapabilities(RESOURCE_1),
                new Stage("stage2")
                        .withChosenResourceCapabilities(RESOURCE_2, RESOURCE_1),
                new Stage("stage3")
                        .withChosenResourceCapabilities(RESOURCE_4));

        //then
        projectCard = projectFacade.load(projectId);
        assertThat(projectCard.parallelizedStages().print()).isIn(
                "stage1 | stage2, stage3",
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
        assertThat(schedule)
                //      .hasStage("stage1").withSlot(JAN_1_2)
                //     .and()
                .hasStage("stage2").withSlot(JAN_2_5)
                .and()
                .hasStage("stage3").withSlot(JAN_2_12);
    }

    void verifyRiskDuringPlanning(ProjectId projectId) {

    }

}