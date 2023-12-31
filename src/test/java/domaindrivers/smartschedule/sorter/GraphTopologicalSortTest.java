package domaindrivers.smartschedule.sorter;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTopologicalSortTest {

    static final GraphTopologicalSort<String> GRAPH_TOPOLOGICAL_SORT = new GraphTopologicalSort<>();

    @Test
    void testTopologicalSortWithSimpleDependencies() {
        //given
        Node<String> node1 = new Node<>("Node1");
        Node<String> node2 = new Node<>("Node2");
        Node<String> node3 = new Node<>("Node3");
        Node<String> node4 = new Node<>("Node4");
        node2 = node2.dependsOn(node1);
        node3 = node3.dependsOn(node1);
        node4 = node4.dependsOn(node2);

        Nodes<String> nodes = new Nodes<>(node1, node2, node3, node4);

        //when
        SortedNodes<String> sortedNodes = GRAPH_TOPOLOGICAL_SORT.apply(nodes);

        //then
        assertEquals(3, sortedNodes.all().size());

        assertEquals(1, sortedNodes.all().get(0).nodes().size());
        assertTrue(sortedNodes.all().get(0).nodes().contains(node1));

        assertEquals(2, sortedNodes.all().get(1).nodes().size());
        assertTrue(sortedNodes.all().get(1).nodes().contains(node2));
        assertTrue(sortedNodes.all().get(1).nodes().contains(node3));

        assertEquals(1, sortedNodes.all().get(2).nodes().size());
        assertTrue(sortedNodes.all().get(2).nodes().contains(node4));
    }

    @Test
    void testTopologicalSortWithLinearDependencies() {
        //given
        Node<String> node1 = new Node<>("Node1");
        Node<String> node2 = new Node<>("Node2");
        Node<String> node3 = new Node<>("Node3");
        Node<String> node4 = new Node<>("Node4");
        Node<String> node5 = new Node<>("Node5");
        node1 = node1.dependsOn(node2);
        node2 = node2.dependsOn(node3);
        node3 = node3.dependsOn(node4);
        node4 = node4.dependsOn(node5);

        Nodes<String> nodes = new Nodes<>(node1, node2, node3, node4, node5);

        //when
        SortedNodes<String> sortedNodes = GRAPH_TOPOLOGICAL_SORT.apply(nodes);

        //then
        assertEquals(5, sortedNodes.all().size());

        assertEquals(1, sortedNodes.all().get(0).nodes().size());
        assertThat(sortedNodes.all().get(0).nodes()).contains(node5);

        assertEquals(1, sortedNodes.all().get(1).nodes().size());
        assertThat(sortedNodes.all().get(1).nodes()).contains(node4);

        assertEquals(1, sortedNodes.all().get(2).nodes().size());
        assertThat(sortedNodes.all().get(2).nodes()).contains(node3);

        assertEquals(1, sortedNodes.all().get(3).nodes().size());
        assertThat(sortedNodes.all().get(3).nodes()).contains(node2);

        assertEquals(1, sortedNodes.all().get(4).nodes().size());
        assertThat(sortedNodes.all().get(4).nodes()).contains(node1);
    }

    @Test
    void testNodesWithoutDependencies() {
        //given
        Node<String> node1 = new Node<>("Node1");
        Node<String> node2 = new Node<>("Node2");
        Nodes<String> nodes = new Nodes<>(Set.of(node1, node2));

        //when
        SortedNodes<String> sortedNodes = GRAPH_TOPOLOGICAL_SORT.apply(nodes);

        //then
        assertEquals(1, sortedNodes.all().size());
    }

    @Test
    void testCyclicDependency() {
        //given
        Node<String> node1 = new Node<>("Node1");
        Node<String> node2 = new Node<>("Node2");
        node2 = node2.dependsOn(node1);
        node1 = node1.dependsOn(node2); // making it cyclic
        Nodes<String> nodes = new Nodes<>(node1, node2);

        //when
        SortedNodes<String> sortedNodes = GRAPH_TOPOLOGICAL_SORT.apply(nodes);

        //then
        assertTrue(sortedNodes.all().isEmpty());
    }

}