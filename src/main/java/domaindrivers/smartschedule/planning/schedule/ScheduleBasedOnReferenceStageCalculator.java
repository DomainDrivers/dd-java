package domaindrivers.smartschedule.planning.schedule;

import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

class ScheduleBasedOnReferenceStageCalculator {

    Map<String, TimeSlot> calculate(Stage referenceStage,
                                    TimeSlot referenceStageProposedTimeSlot,
                                    ParallelStagesList parallelizedStages, Comparator<ParallelStages> comparing) {
        List<ParallelStages> all = parallelizedStages.allSorted(comparing);
        int referenceStageIndex = findReferenceStageIndex(referenceStage, all);
        if (referenceStageIndex == -1) {
            return Map.of();
        }
        Map<String, TimeSlot> scheduleMap = new HashMap<>();
        List<ParallelStages> stagesBeforeReference = all.subList(0, referenceStageIndex);
        List<ParallelStages> stagesAfterReference = all.subList(referenceStageIndex + 1, all.size());
        calculateStagesBeforeCritical(stagesBeforeReference, referenceStageProposedTimeSlot, scheduleMap);
        calculateStagesAfterCritical(stagesAfterReference, referenceStageProposedTimeSlot, scheduleMap);
        calculateStagesWithReferenceStage(all.get(referenceStageIndex), referenceStageProposedTimeSlot, scheduleMap);
        return scheduleMap;
    }

    private Map<String, TimeSlot> calculateStagesBeforeCritical(List<ParallelStages> before, TimeSlot stageProposedTimeSlot, Map<String, TimeSlot> scheduleMap) {
        Instant currentStart = stageProposedTimeSlot.from();
        for (int i = before.size() - 1; i >= 0; i--) {
            ParallelStages currentStages = before.get(i);
            Duration stageDuration = currentStages.duration();
            Instant start = currentStart.minus(stageDuration);
            for (Stage stage : currentStages.stages()) {
                scheduleMap.put(stage.stageName(), new TimeSlot(start, start.plus(stage.duration())));
            }
        }
        return scheduleMap;
    }

    private Map<String, TimeSlot> calculateStagesAfterCritical(List<ParallelStages> after, TimeSlot stageProposedTimeSlot, Map<String, TimeSlot> scheduleMap) {
        Instant currentStart = stageProposedTimeSlot.to();
        for (ParallelStages currentStages : after) {
            for (Stage stage : currentStages.stages()) {
                scheduleMap.put(stage.stageName(), new TimeSlot(currentStart, currentStart.plus(stage.duration())));
            }
            currentStart = currentStart.plus(currentStages.duration());
        }
        return scheduleMap;
    }

    private Map<String, TimeSlot> calculateStagesWithReferenceStage(ParallelStages stagesWithReference, TimeSlot stageProposedTimeSlot, Map<String, TimeSlot> scheduleMap) {
        Instant currentStart = stageProposedTimeSlot.from();
        for (Stage stage : stagesWithReference.stages()) {
            scheduleMap.put(stage.stageName(), new TimeSlot(currentStart, currentStart.plus(stage.duration())));
        }
        return scheduleMap;
    }

    private int findReferenceStageIndex(Stage referenceStage, List<ParallelStages> all) {
        int stagesWithTheReferenceStageWithProposedTimeIndex = -1;
        for (int i = 0; i < all.size(); i++) {
            ParallelStages stages = all.get(i);
            Set<String> stagesNames = stages.stages().stream().map(Stage::stageName).collect(Collectors.toSet());
            if (stagesNames.contains(referenceStage.stageName())) {
                stagesWithTheReferenceStageWithProposedTimeIndex = i;
                break;
            }
        }
        return stagesWithTheReferenceStageWithProposedTimeIndex;
    }
}
