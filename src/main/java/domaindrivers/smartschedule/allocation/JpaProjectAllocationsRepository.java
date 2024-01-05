package domaindrivers.smartschedule.allocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Set;


interface JpaProjectAllocationsRepository extends ProjectAllocationsRepository, JpaRepository<ProjectAllocations, ProjectAllocationsId> {

    @Query(value = "SELECT * FROM project_allocations WHERE from_date <= :when AND to_date > :when",
            nativeQuery = true)
    List<ProjectAllocations> findAllContainingDate(Instant when);

    default List<ProjectAllocations> findAllById(Set<ProjectAllocationsId> projectIds) {
        return this.findAllById(projectIds);
    }

}
