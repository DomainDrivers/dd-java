package domaindrivers.smartschedule.sorter;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public record Nodes(Set<Node> nodes) {

    public Nodes(Node... nodes) {
        this(new HashSet(Arrays.asList(nodes)));
    }

    Set<Node> all() {
        return Collections.unmodifiableSet(nodes);
    }

    Nodes add(Node  node) {
        Set<Node > newNode = concat(this.nodes.stream(), of(node)).collect(toSet());
        return new Nodes(newNode);
    }

    Nodes withAllDependenciesPresentIn(Collection<Node> nodes) {
        return new Nodes(
                all()
                        .stream()
                        .filter(n -> nodes.containsAll(n.dependencies().all()))
                        .collect(toSet()));
    }

    Nodes removeAll(Collection<Node> nodes) {
        return new Nodes(
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

