package domaindrivers.smartschedule.sorter;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;


public record Nodes<T>(Set<Node<T>> nodes) {

    public Nodes(Node<T>... nodes) {
        this(new HashSet<>(Arrays.asList(nodes)));
    }

    Set<Node<T>> all() {
        return Collections.unmodifiableSet(nodes);
    }

    Nodes<T> add(Node<T>  node) {
        Set<Node<T> > newNode = concat(this.nodes.stream(), of(node)).collect(toSet());
        return new Nodes<>(newNode);
    }

    Nodes<T> withAllDependenciesPresentIn(Collection<Node<T>> nodes) {
        return new Nodes<>(
                all()
                        .stream()
                        .filter(n -> nodes.containsAll(n.dependencies().all()))
                        .collect(toSet()));
    }

    Nodes<T> removeAll(Collection<Node<T>> nodes) {
        return new Nodes<>(
                all()
                        .stream()
                        .filter(s -> !nodes.contains(s))
                        .collect(toSet()));
    }

    @Override
    public String toString() {
        return "Nodes{" +
                "node=" + nodes +
                '}';
    }
}

