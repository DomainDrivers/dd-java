package domaindrivers.smartschedule.planning.parallelization;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.*;
import static org.assertj.core.api.Assertions.assertThat;

class DurationCalculatorTest {

    private static final DurationCalculator durationCalculator = new DurationCalculator();

    @Test
    void longestStageIsTakenIntoAccount() {
        //given
        Stage stage1 = new Stage("Stage1").ofDuration(ZERO);
        Stage stage2 = new Stage("Stage2").ofDuration(ofDays(3));
        Stage stage3 = new Stage("Stage3").ofDuration(ofDays(2));
        Stage stage4 = new Stage("Stage4").ofDuration(ofDays(5));

        //when
        Duration duration = durationCalculator.apply(List.of(stage1, stage2, stage3, stage4));

        //then
        assertThat(duration).hasDays(5);
    }

    @Test
    void sumIsTakenIntoAccountWhenNothingIsParallel() {
        //given
        Stage stage1 = new Stage(
                "Stage1").ofDuration(ofHours(10));
        Stage stage2 = new Stage(
                "Stage2").ofDuration(ofHours(24));
        Stage stage3 = new Stage(
                "Stage3").ofDuration(ofDays(2));
        Stage stage4 = new Stage(
                "Stage4").ofDuration(ofDays(1));
        stage4.dependsOn(stage3);
        stage3.dependsOn(stage2);
        stage2.dependsOn(stage1);

        //when
        Duration duration = durationCalculator.apply(List.of(stage1, stage2, stage3, stage4));

        //then
        assertThat(duration).hasHours(106);
    }

}