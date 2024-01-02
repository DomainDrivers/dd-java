package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScheduleBasedOnStartDayCalculator {

    Map<String, TimeSlot> calculate(Instant startDate, ParallelStagesList parallelizedStages, Comparator<ParallelStages> comparing) {
        Map<String, TimeSlot> scheduleMap = new HashMap<>();
        Instant currentStart = startDate;
        List<ParallelStages> allSorted = parallelizedStages.allSorted(comparing);
        //TODO
        return scheduleMap;
    }
}
