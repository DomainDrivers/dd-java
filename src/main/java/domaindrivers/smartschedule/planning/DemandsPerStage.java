package domaindrivers.smartschedule.planning;


import java.util.Map;

record DemandsPerStage(Map<String, Demands> demands) {

    static DemandsPerStage empty() {
        return new DemandsPerStage(Map.of());
    }
}
