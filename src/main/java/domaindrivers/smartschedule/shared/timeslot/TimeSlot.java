package domaindrivers.smartschedule.shared.timeslot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

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

    public boolean within(TimeSlot other) {
        return !this.from.isBefore(other.from) && !this.to.isAfter(other.to);
    }


}
