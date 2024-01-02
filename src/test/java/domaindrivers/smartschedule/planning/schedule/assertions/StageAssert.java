package domaindrivers.smartschedule.planning.schedule.assertions;

import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.planning.schedule.TimeSlot;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.time.Instant;

public class StageAssert extends AbstractAssert<StageAssert, TimeSlot> {

    private final ScheduleAssert scheduleAssert;

    public StageAssert(TimeSlot actual) {
        super(actual, StageAssert.class);
        this.scheduleAssert = null;
    }

    public StageAssert(TimeSlot actual, ScheduleAssert scheduleAssert) {
        super(actual, StageAssert.class);
        this.scheduleAssert = scheduleAssert;
    }

    public StageAssert thatStarts(String start) {
        Assertions.assertThat(actual.from()).isEqualTo(Instant.parse(start));
        return this;
    }

    public StageAssert withSlot(TimeSlot slot) {
        Assertions.assertThat(actual).isEqualTo(slot);
        return this;
    }

    public StageAssert thatEnds(String end) {
        Assertions.assertThat(actual.to()).isEqualTo(Instant.parse(end));
        return this;
    }

    public ScheduleAssert and() {
        return scheduleAssert;
    }

    public StageAssert isBefore(String stage) {
        Schedule schedule = scheduleAssert.schedule();
        Assertions.assertThat(actual.to()).isBeforeOrEqualTo(schedule.dates().get(stage).from());
        return this;
    }

    public StageAssert startsTogetherWith(String stage) {
        Schedule schedule = scheduleAssert.schedule();
        Assertions.assertThat(actual.from()).isEqualTo(schedule.dates().get(stage).from());
        return this;
    }

    public StageAssert isAfter(String stage) {
        Schedule schedule = scheduleAssert.schedule();
        Assertions.assertThat(actual.from()).isAfterOrEqualTo(schedule.dates().get(stage).to());
        return this;
    }
}
