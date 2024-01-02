package domaindrivers.smartschedule.planning.schedule;

import java.time.Duration;
import java.time.Instant;


public record TimeSlot(Instant from, Instant to) {

    static TimeSlot empty() {
        return new TimeSlot(Instant.EPOCH, Instant.EPOCH);
    }

    boolean overlapsWith(TimeSlot other) {
        return !this.from().isAfter(other.to()) && !this.to().isBefore(other.from());
    }


    TimeSlot commonPartWith(TimeSlot other) {
        if (!this.overlapsWith(other)) {
            return TimeSlot.empty();
        }
        Instant commonStart = this.from.isAfter(other.from) ? this.from : other.from;
        Instant commonEnd = this.to.isBefore(other.to) ? this.to : other.to;
        return new TimeSlot(commonStart, commonEnd);
    }

    boolean isEmpty() {
        return this.from.equals(this.to);
    }

    Duration duration() {
        return Duration.between(this.from, this.to);
    }

    TimeSlot stretch(Duration duration) {
        return new TimeSlot(this.from.minus(duration), this.to.plus(duration));
    }
}
