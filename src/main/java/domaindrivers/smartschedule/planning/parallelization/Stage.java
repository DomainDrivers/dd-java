package domaindrivers.smartschedule.planning.parallelization;


import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Stage(String stageName, Set<Stage> dependencies, Set<ResourceName> resources,
                    Duration duration) {

    public Stage(String name) {
        this(name, new HashSet<>(), new HashSet<>(), Duration.ZERO);
    }

    public Stage dependsOn(Stage stage) {
        Set<Stage> newDependencies = new HashSet<>(dependencies);
        newDependencies.add(stage);
        this.dependencies.add(stage);
        return new Stage(stageName, newDependencies, resources, duration);
    }

    public String name() {
        return stageName;
    }

    public Stage withChosenResourceCapabilities(ResourceName... resources) {
        Set<ResourceName> collect = Arrays.stream(resources).collect(Collectors.toSet());
        return new Stage(stageName, dependencies, collect, duration);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceName that = (ResourceName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}