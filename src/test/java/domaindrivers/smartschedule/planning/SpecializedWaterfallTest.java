package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.TaskExecutorConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.Instant;

import static domaindrivers.smartschedule.planning.schedule.assertions.ScheduleAssert.assertThat;


@SpringBootTest
@Import({TestDbConfiguration.class, TaskExecutorConfiguration.class})
@Sql(scripts = {"classpath:schema-risk.sql", "classpath:schema-planning.sql", "classpath:schema-availability.sql", "classpath:schema-resources.sql", "classpath:schema-allocations.sql"})

class SpecializedWaterfallTest {

    static final TimeSlot JAN_1_2 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-02T00:00:00Z"));
    static final TimeSlot JAN_1_4 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-04T00:00:00Z"));
    static final TimeSlot JAN_1_5 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-05T00:00:00Z"));
    static final TimeSlot JAN_1_6 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-06T00:00:00Z"));
    static final TimeSlot JAN_4_8 = new TimeSlot(Instant.parse("2020-01-04T00:00:00.00Z"), Instant.parse("2020-01-08T00:00:00Z"));

    @Autowired
    PlanningFacade projectFacade;

    @Test
    void specializedWaterfallProjectProcess() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("waterfall");
        //and
        Duration criticalStageDuration = Duration.ofDays(5);
        Duration stage1Duration = Duration.ofDays(1);
        Stage stageBeforeCritical = new Stage("stage1").ofDuration(stage1Duration);
        Stage criticalStage = new Stage("stage2").ofDuration(criticalStageDuration);
        Stage stageAfterCritical = new Stage("stage3").ofDuration(Duration.ofDays(3));
        projectFacade.defineProjectStages(projectId, stageBeforeCritical, criticalStage, stageAfterCritical);

        //and
        ResourceId criticalResourceName = ResourceId.newOne();
        ResourceId criticalCapabilityAvailability = resourceAvailableForCapabilityInPeriod(criticalResourceName, Capability.skill("JAVA"), JAN_1_6);

        //when
        projectFacade.planCriticalStageWithResource(projectId, criticalStage, criticalResourceName, JAN_4_8);

        //then
        verifyResourcesNotAvailable(projectId, criticalCapabilityAvailability, JAN_4_8);

        //when
        projectFacade.planCriticalStageWithResource(projectId, criticalStage, criticalResourceName, JAN_1_6);

        //then
        assertResourcesAvailable(projectId, criticalCapabilityAvailability);
        //and
        Schedule schedule = projectFacade.load(projectId).schedule();
        assertThat(schedule)
                .hasStage("stage1").withSlot(JAN_1_2)
                .and()
                .hasStage("stage2").withSlot(JAN_1_6)
                .and()
                .hasStage("stage3").withSlot(JAN_1_4);
    }

    void assertResourcesAvailable(ProjectId projectId, ResourceId resource) {
    }

    void verifyResourcesNotAvailable(ProjectId projectId,  ResourceId resource, TimeSlot requestedButNotAvailable) {
    }

    ResourceId resourceAvailableForCapabilityInPeriod(ResourceId resource, Capability capability, TimeSlot slot) {
        return null;
    }

}