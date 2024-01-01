package domaindrivers.smartschedule.simulation;


import java.util.List;

public record Demands(List<Demand> all) {

    static Demands of(Demand... demands) {
        return new Demands(List.of(demands));
    }

}


