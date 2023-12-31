package domaindrivers.smartschedule.sorter;


import java.util.HashSet;
import java.util.Objects;

public record Node<T>(String name, Nodes<T> dependencies, T content) {
    public Node(String name) {
        this(name, new Nodes<>(new HashSet<>()), null);
    }

    public Node(String name, T content) {
        this(name, new Nodes<>(new HashSet<>()), content);
    }

    public Node<T> dependsOn(Node<T> node) {
        return new Node<>(this.name, this.dependencies.add(node), content);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return name.equals(node.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
