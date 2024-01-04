package domaindrivers.smartschedule.allocation.capabilityscheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AllocatableCapabilityRepository extends JpaRepository<AllocatableCapability, AllocatableCapabilityId> {

    @Query(value = "SELECT ac.*\n" +
            "FROM allocatable_capabilities ac \n" +
            "CROSS JOIN LATERAL jsonb_array_elements(ac.possible_capabilities -> 'capabilities') AS o(obj)\n" +
            "WHERE o.obj ->> 'name' = ?1 AND o.obj ->> 'type' = ?2 AND ac.from_date <= ?3 and ac.to_date >= ?4", nativeQuery = true)
    List<AllocatableCapability> findByCapabilityWithin(String name, String type, Instant from, Instant to);


    @Query(value = "SELECT ac.*\n" +
            "FROM allocatable_capabilities ac \n" +
            "CROSS JOIN LATERAL jsonb_array_elements(ac.possible_capabilities -> 'capabilities') AS o(obj)\n" +
            "WHERE ac.resource_id = ?1 AND o.obj ->> 'name' = ?2 AND o.obj ->> 'type' = ?3 AND ac.from_date = ?4 and ac.to_date = ?5", nativeQuery = true)
    Optional<AllocatableCapability> findByResourceIdAndCapabilityAndTimeSlot(UUID allocatableResourceId, String name, String type, Instant from, Instant to);

    @Query(value = "SELECT ac.*\n" +
            "FROM allocatable_capabilities ac \n" +
            "WHERE ac.resource_id = ?1 AND ac.from_date = ?2 and ac.to_date = ?3", nativeQuery = true)
    List<AllocatableCapability> findByResourceIdAndTimeSlot(UUID allocatableResourceId, Instant from, Instant to);
}
