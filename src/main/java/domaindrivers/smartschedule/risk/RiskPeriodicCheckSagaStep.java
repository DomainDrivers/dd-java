package domaindrivers.smartschedule.risk;

public enum RiskPeriodicCheckSagaStep {
    FIND_AVAILABLE,
    DO_NOTHING,
    SUGGEST_REPLACEMENT,
    NOTIFY_ABOUT_POSSIBLE_RISK,
    NOTIFY_ABOUT_DEMANDS_SATISFIED
}
