package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.Edge;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyRemovalSuggesting {

    static final StageParallelization stageParallelization = new StageParallelization();


    @Test
    void suggestingBreaksTheCycleInSchedule() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        Stage stage3 = new Stage("Stage3");
        Stage stage4 = new Stage("Stage4");
        stage1 = stage1.dependsOn(stage2);
        stage2 = stage2.dependsOn(stage3);
        stage4 = stage4.dependsOn(stage3);
        stage1 = stage1.dependsOn(stage4);
        stage3 = stage3.dependsOn(stage1);

        //when
        RemovalSuggestion suggestion = stageParallelization.whatToRemove(Set.of(stage1, stage2, stage3, stage4));

        //then
        assertThat(suggestion.toString()).isEqualTo("[(3 -> 1), (4 -> 3)]");
    }
}