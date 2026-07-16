package domaindrivers.smartschedule.shared;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface OutboxRepository extends JpaRepository<OutboxRecord, UUID> {

    List<OutboxRecord> findBySentAtIsNull();
}
