package domaindrivers.smartschedule.allocation;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProjectAllocationsId implements Serializable {

    public static ProjectAllocationsId newOne() {
        return new ProjectAllocationsId(UUID.randomUUID());
    }

    private UUID projectAllocationsId;

    public ProjectAllocationsId(UUID uuid) {
        this.projectAllocationsId = uuid;
    }

    public ProjectAllocationsId() {
    }

    public UUID id() {
        return projectAllocationsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectAllocationsId projectId1 = (ProjectAllocationsId) o;
        return Objects.equals(projectAllocationsId, projectId1.projectAllocationsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectAllocationsId);
    }
}

