package domaindrivers.smartschedule.planning;


import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import org.hibernate.annotations.Type;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RedisHash("projects")
class Project {

    private String id;

    private String name;

    private ParallelStagesList parallelizedStages;

    private DemandsPerStage demandsPerStage;

    private Demands allDemands;

    private ChosenResources chosenResources;

    private Schedule schedule;

    Project(String name, ParallelStagesList parallelizedStages) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.parallelizedStages = parallelizedStages;
        this.allDemands = Demands.none();
        this.schedule = Schedule.none();
    }

    public Project() {
    }

    void addDemands(Demands demands) {
        this.allDemands = this.allDemands.add(demands);
    }

    Demands getAllDemands() {
        return allDemands;
    }

    ParallelStagesList getParallelizedStages() {
        return parallelizedStages;
    }

    void addSchedule(Instant possibleStartDate) {
        this.schedule = Schedule.basedOnStartDay(possibleStartDate, parallelizedStages);
    }

    void addChosenResources(ChosenResources neededResources) {
        this.chosenResources = neededResources;
    }

    ChosenResources getChosenResources() {
        return chosenResources;
    }

    void addDemandsPerStage(DemandsPerStage demandsPerStage) {
        this.demandsPerStage = demandsPerStage;
        Set<Demand> uniqueDemands = demandsPerStage.demands().values()
                        .stream()
                                .flatMap(demands -> demands.all().stream())
                                        .collect(Collectors.toSet());
        addDemands(new Demands(new ArrayList<>(uniqueDemands)));
    }

    DemandsPerStage getDemandsPerStage() {
        return demandsPerStage;
    }

    void addSchedule(Stage criticalStage, TimeSlot stageTimeSlot) {
        this.schedule = Schedule.basedOnReferenceStageTimeSlot(criticalStage, stageTimeSlot, parallelizedStages);
    }
    void addSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    Schedule getSchedule() {
        return schedule;
    }

    void defineStages(ParallelStagesList parallelizedStages) {
        this.parallelizedStages = parallelizedStages;
    }

    String name() {
        return name;
    }

    ProjectId id() {
        return new ProjectId(UUID.fromString(id));
    }

}

