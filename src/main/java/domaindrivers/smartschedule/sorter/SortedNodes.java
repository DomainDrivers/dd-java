package domaindrivers.smartschedule.sorter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SortedNodes(List<Nodes> all) {

    public static SortedNodes empty() {
        return new SortedNodes(new ArrayList<>());
    }

    public SortedNodes add(Nodes newNodes) {
        List<Nodes> result =
                Stream
                        .concat(this.all.stream(), Stream.of(newNodes))
                        .collect(Collectors.toList());
        return new SortedNodes(result);
    }

    @Override
    public String toString() {
        return "SortedNodes: " + all;
    }
}
