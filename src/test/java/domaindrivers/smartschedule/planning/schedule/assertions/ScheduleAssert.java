package domaindrivers.smartschedule.planning.schedule.assertions;

import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.planning.schedule.TimeSlot;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class ScheduleAssert extends AbstractAssert<ScheduleAssert, Schedule> {

    public ScheduleAssert(Schedule actual) {
        super(actual, ScheduleAssert.class);
    }

    public static ScheduleAssert assertThat(Schedule actual) {
        return new ScheduleAssert(actual);
    }

    public ScheduleAssert hasStages(int number) {
        Assertions.assertThat(actual.dates().keySet()).hasSize(number);
        return this;
    }

    public StageAssert hasStage(String name) {
        TimeSlot stageTimeSlot = actual.dates().get(name);
        Assertions.assertThat(stageTimeSlot).isNotNull();
        return new StageAssert(stageTimeSlot, this);
    }

    public void isEmpty() {
        Assertions.assertThat(actual).isEqualTo(Schedule.none());
    }

    public Schedule schedule() {
        return actual;
    }
}


