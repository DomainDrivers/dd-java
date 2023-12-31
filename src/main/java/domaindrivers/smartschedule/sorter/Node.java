package domaindrivers.smartschedule.sorter;

import domaindrivers.smartschedule.planning.parallelization.Stage;

import java.util.HashSet;
import java.util.Objects;

public record Node(String name, Nodes dependencies, Stage content) {
    public Node(String name) {
        this(name, new Nodes(new HashSet<>()), null);
    }

    public Node(String name, Stage content) {
        this(name, new Nodes(new HashSet<>()), content);
    }

    public Node dependsOn(Node node) {
        return new Node(this.name, this.dependencies.add(node), content);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return name.equals(node.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
