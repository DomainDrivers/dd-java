package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.sql.Timestamp.from;

class ResourceAvailabilityReadModel {

    String calendar_query = """
            WITH AvailabilityWithLag AS (
                SELECT
                    resource_id,
                    taken_by,
                    from_date,
                    to_date,
                    COALESCE(LAG(to_date) OVER (PARTITION BY resource_id, taken_by ORDER BY from_date), from_date) AS prev_to_date
                FROM  
                    availabilities
                WHERE
                    from_date >= ? 
                    AND to_date <= ?
                    AND resource_id = ANY (?)
                
            ),
            GroupedAvailability AS (
                SELECT
                    resource_id,
                    taken_by,
                    from_date,
                    to_date,
                    prev_to_date,
                    CASE WHEN
                        from_date = prev_to_date
                        THEN 0 ELSE 1 END
                    AS new_group_flag,
                    SUM(CASE WHEN
                        from_date = prev_to_date
                        THEN 0 ELSE 1 END)
                    OVER (PARTITION BY resource_id, taken_by ORDER BY from_date) AS grp
                FROM  
                    AvailabilityWithLag
            )
            SELECT
                resource_id,
                taken_by,
                MIN(from_date) AS start_date,
                MAX(to_date) AS end_date
            FROM
                GroupedAvailability
            GROUP BY
                resource_id, taken_by, grp
            ORDER BY
                start_date;
             """;

    private final JdbcTemplate jdbcTemplate;

    ResourceAvailabilityReadModel(JdbcTemplate client) {
        this.jdbcTemplate = client;
    }

    Calendar load(ResourceId resourceId, TimeSlot timeSlot) {
        Calendars loaded = loadAll(Set.of(resourceId), timeSlot);
        return loaded.get(resourceId);
    }

    Calendars loadAll(Set<ResourceId> resourceIds, TimeSlot timeSlot) {
        UUID[] ids = resourceIds.stream().map(ResourceId::getId).toArray(  UUID[]::new);
        List<Map<String, Object>> results =
                jdbcTemplate.queryForList(
                        calendar_query,
                        from(timeSlot.from()),
                        from(timeSlot.to()),
                        ids);
        Map<ResourceId, Map<Owner, List<TimeSlot>>> calendars = new HashMap<>();
        for (Map<String, Object> row : results) {
            UUID resource = (UUID) row.get("resource_id");
            ResourceId key = new ResourceId(resource);
            UUID takenByUuid = (UUID) row.get("taken_by");
            Owner takenBy = (takenByUuid == null) ? Owner.none() : new Owner(takenByUuid);
            Instant fromDate = ((Timestamp) row.get("start_date")).toInstant();
            Instant toDate = ((Timestamp) row.get("end_date")).toInstant();
            TimeSlot loadedSlot = new TimeSlot(fromDate, toDate);
            calendars.computeIfAbsent(key, res -> new HashMap<>());
            calendars.get(key).computeIfAbsent(takenBy, owner -> new ArrayList<>()).add(loadedSlot);
        }
        return new Calendars(calendars
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new Calendar(e.getKey(), e.getValue()))));
    }


}



