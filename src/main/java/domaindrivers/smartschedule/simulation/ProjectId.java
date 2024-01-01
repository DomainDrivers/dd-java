package domaindrivers.smartschedule.simulation;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ProjectId implements Serializable {

    public static ProjectId newOne() {
        return new ProjectId(UUID.randomUUID());
    }

    private UUID projectId;

    ProjectId(UUID uuid) {
        this.projectId = uuid;
    }

    public UUID id() {
        return projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectId projectId1 = (ProjectId) o;
        return Objects.equals(projectId, projectId1.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

}

