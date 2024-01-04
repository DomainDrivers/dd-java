package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.sql.Timestamp.from;

class ResourceAvailabilityRepository {

    private final JdbcTemplate jdbcTemplate;

    ResourceAvailabilityRepository(JdbcTemplate client) {
        this.jdbcTemplate = client;
    }

    void saveNew(ResourceAvailability resourceAvailability) {
        saveNew(List.of(resourceAvailability));
    }

    void saveNew(ResourceGroupedAvailability groupedAvailability) {
        saveNew(groupedAvailability.availabilities());
    }

    private void saveNew(List<ResourceAvailability> availabilities) {
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO  availabilities 
                        (id, resource_id, resource_parent_id, from_date, to_date, taken_by, disabled, version) 
                        VALUES 
                        (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                availabilities,
                100,
                (PreparedStatement ps, ResourceAvailability ra) -> {
                    ps.setObject(1, ra.id().id());
                    ps.setObject(2, ra.resourceId().getId());
                    ps.setObject(3, ra.resourceParentId().getId());
                    ps.setTimestamp(4, from(ra.segment().from()));
                    ps.setTimestamp(5, from(ra.segment().to()));
                    ps.setObject(6, null);
                    ps.setBoolean(7, false);
                    ps.setInt(8, 0);
                });
    }

    List<ResourceAvailability> loadAllWithinSlot(ResourceId resourceId, TimeSlot segment) {
        return jdbcTemplate
                .query("""
                                select * from availabilities where resource_id = ? 
                                and from_date >= ? and to_date <= ?
                                """, ResourceAvailabilityRowMapper.rowMapper,
                        resourceId.getId(), from(segment.from()), from(segment.to()));
    }

    List<ResourceAvailability> loadAllByParentIdWithinSlot(ResourceId parentId, TimeSlot segment) {
        return jdbcTemplate
                .query("""
                                select * from availabilities where resource_parent_id = ? 
                                and from_date >= ? and to_date <= ?
                                """, ResourceAvailabilityRowMapper.rowMapper,
                        parentId.getId(), from(segment.from()), from(segment.to()));
    }

    boolean saveCheckingVersion(ResourceAvailability resourceAvailability) {
        UUID id = resourceAvailability.id().id();
        int version = resourceAvailability.version();
        int update = jdbcTemplate
                .update("""
                                UPDATE availabilities 
                                SET taken_by = ?, disabled = ?, version = ? 
                                WHERE id = ? AND version = ?
                                """,
                        resourceAvailability.blockedBy().id(), resourceAvailability.isDisabled(), version + 1, id, version);
        return update == 1;
    }

    boolean saveCheckingVersion(ResourceGroupedAvailability groupedAvailability) {
        return saveCheckingVersion(groupedAvailability.availabilities());
    }

    boolean saveCheckingVersion(List<ResourceAvailability> resourceAvailabilities) {
        int[][] results = jdbcTemplate.batchUpdate("""
                        UPDATE availabilities 
                        SET taken_by = ?, disabled = ?, version = ? 
                        WHERE id = ? AND version = ?
                        """,
                resourceAvailabilities,
                100,
                (PreparedStatement ps, ResourceAvailability ra) -> {
                    ps.setObject(1, ra.blockedBy().id());
                    ps.setObject(2, ra.isDisabled());
                    ps.setInt(3, ra.version() + 1);
                    ps.setObject(4, ra.id().id());
                    ps.setInt(5, ra.version());
                });
        return Stream.of(results).flatMapToInt(IntStream::of).allMatch(i -> i == 1);
    }

    public ResourceAvailability loadById(ResourceAvailabilityId availabilityId) {
        return jdbcTemplate
                .queryForObject("select * from availabilities where id = ?",
                        ResourceAvailabilityRowMapper.rowMapper,
                        availabilityId.id());
    }

    public ResourceGroupedAvailability loadAvailabilitiesOfRandomResourceWithin(Set<ResourceId> resourceIds, TimeSlot normalized) {
        UUID[] ids = resourceIds.stream().map(ResourceId::getId).toArray(UUID[]::new);
        List<ResourceAvailability> availabilities = jdbcTemplate
                .query("WITH AvailableResources AS (" +
                                "                        SELECT resource_id" +
                                "                        FROM availabilities" +
                                "                        WHERE resource_id = ANY(?::uuid[])" +
                                "        AND taken_by IS NULL" +
                                "        AND from_date >= ?" +
                                "        AND to_date <= ?" +
                                "        GROUP BY resource_id" +
                                ")," +
                                "        RandomResource AS (" +
                                "                SELECT resource_id" +
                                "        FROM AvailableResources" +
                                "        ORDER BY RANDOM()" +
                                "        LIMIT 1" +
                                ")" +
                                "        SELECT a.*" +
                                "        FROM availabilities a" +
                                "        JOIN RandomResource r ON a.resource_id = r.resource_id",
                        ResourceAvailabilityRowMapper.rowMapper,
                        ids,
                        from(normalized.from()),
                        from(normalized.to()));
        return new ResourceGroupedAvailability(availabilities);
    }
}

class ResourceAvailabilityRowMapper {

    static RowMapper<ResourceAvailability> rowMapper =
            (rs, rowNum) -> {
                ResourceAvailabilityId resourceAvailabilityId = ResourceAvailabilityId.of(rs.getString("id"));
                ResourceId resourceId = ResourceId.of(rs.getString("resource_id"));
                TimeSlot segment = new TimeSlot(rs.getTimestamp("from_date").toInstant(), rs.getTimestamp("to_date").toInstant());
                ResourceId parentId = ResourceId.of(rs.getString("resource_parent_id"));
                boolean isDisabled = rs.getBoolean("disabled");
                Owner result;
                String id = rs.getString("taken_by");
                if (id == null) {
                    result = Owner.none();
                } else {
                    result = new Owner(UUID.fromString(id));
                }
                Blockade blocade = new Blockade(result, isDisabled);
                int version = rs.getInt("version");
                return new ResourceAvailability(resourceAvailabilityId, resourceId, parentId, segment, blocade, version);
            };
}
