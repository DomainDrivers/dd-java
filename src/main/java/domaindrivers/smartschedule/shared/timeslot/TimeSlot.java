package domaindrivers.smartschedule.shared.timeslot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public record TimeSlot(Instant from, Instant to) {

    public static TimeSlot createDailyTimeSlotAtUTC(int year, int month, int day) {
        LocalDate thisDay = LocalDate.of(year, month, day);
        Instant from = thisDay.atStartOfDay(ZoneId.of("UTC")).toInstant();
        return new TimeSlot(from, from.plus(1, DAYS));
    }

    public static TimeSlot createMonthlyTimeSlotAtUTC(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);
        Instant from = startOfMonth.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant to = endOfMonth.atTime(0, 0, 0).atZone(ZoneId.of("UTC")).toInstant();
        return new TimeSlot(from, to);
    }

    boolean overlapsWith(TimeSlot other) {
        return !this.from().isAfter(other.to()) && !this.to().isBefore(other.from());
    }

    public boolean within(TimeSlot other) {
        return !this.from.isBefore(other.from) && !this.to.isAfter(other.to);
    }

    public List<TimeSlot> leftoverAfterRemovingCommonWith(TimeSlot other) {
        List<TimeSlot> result = new ArrayList<>();
        if (other.equals(this)) {
            return List.of();
        }
        if (!other.overlapsWith(this)) {
            return List.of(this, other);
        }
        if (this.equals(other)) {
            return result;
        }
        if (this.from.isBefore(other.from)) {
            result.add(new TimeSlot(this.from, other.from));
        }
        if (other.from.isBefore(this.from)) {
            result.add(new TimeSlot(other.from, this.from));
        }
        if (this.to.isAfter(other.to)) {
            result.add(new TimeSlot(other.to, this.to));
        }
        if (other.to.isAfter(this.to)) {
            result.add(new TimeSlot(this.to, other.to));
        }
        return result;
    }
}
