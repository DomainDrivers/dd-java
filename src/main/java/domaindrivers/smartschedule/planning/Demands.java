package domaindrivers.smartschedule.planning;


import java.util.List;
import java.util.stream.Stream;

record Demands(List<Demand> all) {
    static Demands none() {
        return new Demands(List.of());
    }

    static Demands of(Demand... demands) {
        return new Demands(List.of(demands));
    }

    Demands add(Demands demands) {
        return new Demands(Stream.concat(all.stream(), demands.all.stream()).toList());
    }
}
