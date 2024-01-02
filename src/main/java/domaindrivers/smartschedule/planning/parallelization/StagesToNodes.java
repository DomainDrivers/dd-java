package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.Node;
import domaindrivers.smartschedule.sorter.Nodes;

import java.util.*;
import java.util.stream.Collectors;

class StagesToNodes {

    Nodes calculate(List<Stage> stages) {
        Map<String, Node> result = stages.stream()
                .collect(Collectors.toMap(Stage::name, stage -> new Node(stage.name(), stage)));

        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            result = explicitDependencies(stage, result);
        }

        return new Nodes(new HashSet<>(result.values()));
    }

    private Map<String, Node> explicitDependencies(Stage stage, Map<String, Node> result) {
        Node nodeWithExplicitDeps = result.get(stage.name());
        for(Stage explicitDependency: stage.dependencies()) {
            nodeWithExplicitDeps = nodeWithExplicitDeps.dependsOn(result.get(explicitDependency.name()));
        }
        result.put(stage.name(), nodeWithExplicitDeps);
        return result;
    }
}

