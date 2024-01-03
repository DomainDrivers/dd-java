package domaindrivers.smartschedule.sorter;

public record Edge(int source, int target) {
    @Override
    public String toString() {
        return "(" + source + " -> " + target + ")";
    }
}
