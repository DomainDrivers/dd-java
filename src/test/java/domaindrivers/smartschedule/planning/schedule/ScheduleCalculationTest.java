package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.availability.Calendar;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static domaindrivers.smartschedule.planning.schedule.assertions.ScheduleAssert.assertThat;
import static java.time.Duration.ofDays;
import static java.time.Instant.parse;

class ScheduleCalculationTest {

    static final Instant JAN_1 = parse("2020-01-01T00:00:00.00Z");
    static final TimeSlot JAN_10_20 = new TimeSlot(parse("2020-01-10T00:00:00.00Z"), parse("2020-01-20T00:00:00.00Z"));
    static final TimeSlot JAN_1_1 = new TimeSlot(parse("2020-01-01T00:00:00.00Z"), parse("2020-01-02T00:00:00.00Z"));
    static final TimeSlot JAN_3_10 = new TimeSlot(parse("2020-01-03T00:00:00.00Z"), parse("2020-01-10T00:00:00.00Z"));
    static final TimeSlot JAN_1_20 = new TimeSlot(parse("2020-01-01T00:00:00.00Z"), parse("2020-01-20T00:00:00.00Z"));
    static final TimeSlot JAN_11_21 = new TimeSlot(parse("2020-01-11T00:00:00.00Z"), parse("2020-01-21T00:00:00.00Z"));
    static final TimeSlot JAN_1_4 = new TimeSlot(parse("2020-01-01T00:00:00.00Z"), parse("2020-01-04T00:00:00.00Z"));
    static final TimeSlot JAN_4_14 = new TimeSlot(parse("2020-01-04T00:00:00.00Z"), parse("2020-01-14T00:00:00.00Z"));
    static final TimeSlot JAN_14_16 = new TimeSlot(parse("2020-01-14T00:00:00.00Z"), parse("2020-01-16T00:00:00.00Z"));
    static final TimeSlot JAN_1_5 = new TimeSlot(parse("2020-01-01T00:00:00.00Z"), parse("2020-01-05T00:00:00.00Z"));
    static final TimeSlot DEC_29_JAN_1 = new TimeSlot(parse("2019-12-29T00:00:00.00Z"), parse("2020-01-01T00:00:00.00Z"));
    static final TimeSlot JAN_1_11 = new TimeSlot(parse("2020-01-01T00:00:00.00Z"), parse("2020-01-11T00:00:00.00Z"));
    static final TimeSlot JAN_5_7 = new TimeSlot(parse("2020-01-05T00:00:00.00Z"), parse("2020-01-07T00:00:00.00Z"));
    static final TimeSlot JAN_3_6 = new TimeSlot(parse("2020-01-03T00:00:00.00Z"), parse("2020-01-06T00:00:00.00Z"));

    @Test
    void canCalculateScheduleBasedOnTheStartDay() {
        //given
        Stage stage1 = new Stage("Stage1").ofDuration(ofDays(3));
        Stage stage2 = new Stage("Stage2").ofDuration(ofDays(10));
        Stage stage3 = new Stage("Stage3").ofDuration(ofDays(2));
        //and
        ParallelStagesList parallelStages =
                ParallelStagesList.of(
                        ParallelStages.of(stage1),
                        ParallelStages.of(stage2),
                        ParallelStages.of(stage3));

        //when
        Schedule schedule = Schedule.basedOnStartDay(JAN_1, parallelStages);

        //then
        assertThat(schedule)
                .hasStage("Stage1").withSlot(JAN_1_4)
                .and()
                .hasStage("Stage2").withSlot(JAN_4_14)
                .and()
                .hasStage("Stage3").withSlot(JAN_14_16);
    }

    @Test
    void scheduleCanAdjustToDatesOfOneReferenceStage() {
        //given
        Stage stage = new Stage("S1").ofDuration(ofDays(3));
        Stage anotherStage = new Stage("S2").ofDuration(ofDays(10));
        Stage yetAnotherStage = new Stage("S3").ofDuration(ofDays(2));
        Stage referenceStage = new Stage("S4-Reference").ofDuration(ofDays(4));
        //and
        ParallelStagesList parallelStages =
                ParallelStagesList.of(
                        ParallelStages.of(stage),
                        ParallelStages.of(referenceStage, anotherStage),
                        ParallelStages.of(yetAnotherStage));

        //when
        Schedule schedule = Schedule.basedOnReferenceStageTimeSlot(referenceStage, JAN_1_5, parallelStages);

        //then
        assertThat(schedule)
                .hasStage("S1").withSlot(DEC_29_JAN_1).isBefore("S4-Reference")
                .and()
                .hasStage("S2").withSlot(JAN_1_11).startsTogetherWith("S4-Reference")
                .and()
                .hasStage("S3").withSlot(JAN_5_7).isAfter("S4-Reference")
                .and()
                .hasStage("S4-Reference").withSlot(JAN_1_5);
    }

    @Test
    void noScheduleIsCalculatedIfReferenceStageToAdjustToDoesNotExists() {
        //given
        Stage stage1 = new Stage("Stage1").ofDuration(ofDays(3));
        Stage stage2 = new Stage("Stage2").ofDuration(ofDays(10));
        Stage stage3 = new Stage("Stage3").ofDuration(ofDays(2));
        Stage stage4 = new Stage("Stage4").ofDuration(ofDays(4));
        //and
        ParallelStagesList parallelStages =
                ParallelStagesList.of(
                        ParallelStages.of(stage1),
                        ParallelStages.of(stage2, stage4),
                        ParallelStages.of(stage3));

        //when
        Schedule schedule = Schedule.basedOnReferenceStageTimeSlot(new Stage("Stage5"), JAN_1_5, parallelStages);

        //then
        assertThat(schedule).isEmpty();
    }

    @Test
    void canAdjustScheduleToAvailabilityOfNeededResources() {
        //given
        ResourceName r1 = new ResourceName("r1");
        ResourceName r2 = new ResourceName("r2");
        ResourceName r3 = new ResourceName("r3");
        //and
        Stage stage1 = new Stage("Stage1")
                .ofDuration(ofDays(3))
                .withChosenResourceCapabilities(r1);
        Stage stage2 = new Stage("Stage2")
                .ofDuration(ofDays(10))
                .withChosenResourceCapabilities(r2, r3);
        //and
        Calendar cal1 = Calendar.withAvailableSlots(r1, JAN_1_1, JAN_3_10);
        Calendar cal2 = Calendar.withAvailableSlots(r2, JAN_1_20);
        Calendar cal3 = Calendar.withAvailableSlots(r3, JAN_11_21);

        //when
        Schedule schedule = Schedule
                .basedOnChosenResourcesAvailability(Calendars.of(cal1, cal2, cal3), List.of(stage1, stage2));

        //then
        assertThat(schedule)
                .hasStage("Stage1")
                .withSlot(JAN_3_6)
                .and()
                .hasStage("Stage2")
                .withSlot(JAN_10_20);
    }

}