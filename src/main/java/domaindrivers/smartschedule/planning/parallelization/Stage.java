package domaindrivers.smartschedule.planning.parallelization;


import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Stage(String stageName, Set<Stage> dependencies, Set<ResourceName> resources,
                    Duration duration) {

    public Stage(String name) {
        this(name, new HashSet<>(), new HashSet<>(), Duration.ZERO);
    }

    public Stage dependsOn(Stage stage) {
        this.dependencies.add(stage);
        return this;
    }

    public String name() {
        return stageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stage stage = (Stage) o;
        return Objects.equals(stageName, stage.stageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stageName);
    }
}

record ResourceName(String name) {

}