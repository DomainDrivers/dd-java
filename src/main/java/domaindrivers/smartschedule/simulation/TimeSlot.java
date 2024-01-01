package domaindrivers.smartschedule.simulation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.time.temporal.ChronoUnit.DAYS;

record TimeSlot(Instant from, Instant to) {

    static TimeSlot createDailyTimeSlotAtUTC(int year, int month, int day) {
        LocalDate thisDay = LocalDate.of(year, month, day);
        Instant from = thisDay.atStartOfDay(ZoneId.of("UTC")).toInstant();
        return new TimeSlot(from, from.plus(1, DAYS));
    }

    boolean within(TimeSlot other) {
        return !this.from.isBefore(other.from) && !this.to.isAfter(other.to);
    }


}
