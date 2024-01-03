package domaindrivers.smartschedule.sorter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class FeedbackArcSetOnGraphTest {

    @Test
    void canFindMinimumNumberOfEdgesToRemoveToMakeTheGraphAcyclic() {
        //given
        Node<String> node1 = new Node<>("1");
        Node<String> node2 = new Node<>("2");
        Node<String> node3 = new Node<>("3");
        Node<String> node4 = new Node<>("4");
        node1 = node1.dependsOn(node2);
        node2 = node2.dependsOn(node3);
        node4 = node4.dependsOn(node3);
        node1 = node1.dependsOn(node4);
        node3 = node3.dependsOn(node1);

        //when
        List<Edge> toRemove = new FeedbackArcSeOnGraph<String>().calculate(List.of(node1, node2, node3, node4));

        //then
        assertThat(toRemove).containsExactlyInAnyOrder(
                new Edge(3, 1),
                new Edge(4, 3)
        );
    }

    @Test
    void whenGraphIsAcyclicThereIsNothingToRemove() {
        //given
        Node<String> node1 = new Node<>("1");
        Node<String> node2 = new Node<>("2");
        Node<String> node3 = new Node<>("3");
        Node<String> node4 = new Node<>("4");
        node1 = node1.dependsOn(node2);
        node2 = node2.dependsOn(node3);
        node1 = node1.dependsOn(node4);

        //when
        List<Edge> toRemove = new FeedbackArcSeOnGraph<String>().calculate(List.of(node1, node2, node3, node4));

        //then
        assertThat(toRemove).isEmpty();
    }

}