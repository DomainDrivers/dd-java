package domaindrivers.smartschedule.sorter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SortedNodes<T>(List<Nodes<T>> all) {

    public static <T> SortedNodes<T> empty() {
        return new SortedNodes<>(new ArrayList<>());
    }

    public SortedNodes<T> add(Nodes<T> newNodes) {
        List<Nodes<T>> result =
                Stream
                        .concat(this.all.stream(), Stream.of(newNodes))
                        .collect(Collectors.toList());
        return new SortedNodes<>(result);
    }

    @Override
    public String toString() {
        return "SortedNodes: " + all;
    }
}