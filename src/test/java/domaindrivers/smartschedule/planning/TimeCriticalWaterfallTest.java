package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.TaskExecutorConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.Ignore;
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
@Sql(scripts = {"classpath:schema-risk.sql", "classpath:schema-availability.sql", "classpath:schema-resources.sql", "classpath:schema-allocations.sql"})

@Ignore
class TimeCriticalWaterfallTest {

    static final TimeSlot JAN_1_5 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-05T00:00:00.00Z"));
    static final TimeSlot JAN_1_3 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-03T00:00:00Z"));
    static final TimeSlot JAN_1_4 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-04T00:00:00Z"));

    @Autowired
    PlanningFacade projectFacade;

    @Test
    void timeCriticalWaterfallProjectProcess() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("waterfall");
        //and
        Stage stageBeforeCritical = new Stage("stage1")
                .ofDuration(Duration.ofDays(2));
        Stage criticalStage = new Stage("stage2")
                .ofDuration(JAN_1_5.duration());
        Stage stageAfterCritical = new Stage("stage3")
                .ofDuration(Duration.ofDays(3));
        projectFacade.defineProjectStages(projectId, stageBeforeCritical, criticalStage, stageAfterCritical);

        //when
        projectFacade.planCriticalStage(projectId, criticalStage, JAN_1_5);

        //then
        Schedule schedule = projectFacade.load(projectId).schedule();
        assertThat(schedule)
                .hasStage("stage1").withSlot(JAN_1_3)
                .and()
                .hasStage("stage2").withSlot(JAN_1_5)
                .and()
                .hasStage("stage3").withSlot(JAN_1_4);
    }

}