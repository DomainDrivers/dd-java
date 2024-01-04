package domaindrivers.smartschedule.allocation.capabilityscheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AllocatableCapabilityRepository extends JpaRepository<AllocatableCapability, AllocatableCapabilityId> {

        @Query(value = "SELECT ac.*\n" +
                "FROM allocatable_capabilities ac \n" +
                "WHERE ac.capability ->> 'name' = ?1 AND ac.capability ->> 'type' = ?2 AND ac.from_date <= ?3 and ac.to_date >= ?4", nativeQuery = true)
        List<AllocatableCapability> findByCapabilityWithin(String name, String type, Instant from, Instant to);


}
