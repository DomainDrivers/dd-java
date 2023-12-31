package domaindrivers.smartschedule.planning.parallelization;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelizationTest {

    static final StageParallelization stageParallelization = new StageParallelization();

    static final ResourceName LEON = new ResourceName("Leon");
    static final ResourceName ERYK = new ResourceName("Eric");
    static final ResourceName SLAWEK = new ResourceName("Sławek");
    static final ResourceName KUBA = new ResourceName("Kuba");

    @Test
    void everythingCanBeDoneInParallelWhenThereAreNoDependencies() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2));

        //then
        assertEquals(1, sortedStages.all().size());
    }

    @Test
    void testSimpleDependencies() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        Stage stage3 = new Stage("Stage3");
        Stage stage4 = new Stage("Stage4");
        stage2 = stage2.dependsOn(stage1);
        stage3 = stage3.dependsOn(stage1);
        stage4 = stage4.dependsOn(stage2);

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2, stage3, stage4));

        //then
        assertEquals(sortedStages.print(), "Stage1 | Stage2, Stage3 | Stage4");
    }

    @Test
    void cantBeDoneWhenThereIsACycle() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        stage2 = stage2.dependsOn(stage1);
        stage1 = stage1.dependsOn(stage2); // making it cyclic

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2));

        //then
        assertTrue(sortedStages.all().isEmpty());
    }

    @Test
    void takesIntoAccountSharedResources() {
        //given
        Stage stage1 = new Stage(
                "Stage1")
                .withChosenResourceCapabilities(
                        LEON);
        Stage stage2 = new Stage(
                "Stage2")
                .withChosenResourceCapabilities(
                        ERYK, LEON);
        Stage stage3 = new Stage(
                "Stage3")
                .withChosenResourceCapabilities(
                        SLAWEK);
        Stage stage4 = new Stage(
                "Stage4")
                .withChosenResourceCapabilities(
                        SLAWEK, KUBA);

        //when
        ParallelStagesList parallelStages = stageParallelization.of(Set.of(stage1, stage2, stage3, stage4));

        //then
        assertThat(parallelStages.print()).isIn(
                "Stage1, Stage3 | Stage2, Stage4",
                        "Stage2, Stage4 | Stage1, Stage3");
    }

}