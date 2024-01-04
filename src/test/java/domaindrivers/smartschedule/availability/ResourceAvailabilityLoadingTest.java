package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
public class ResourceAvailabilityLoadingTest {

    static final TimeSlot ONE_MONTH = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

    @Autowired
    JdbcTemplate jdbcTemplate;

    ResourceAvailabilityRepository resourceAvailabilityRepository;

    @BeforeEach
    void setup() {
        resourceAvailabilityRepository = new ResourceAvailabilityRepository(jdbcTemplate);
    }

    @Test
    void canSaveAndLoadById() {
        //given
        ResourceAvailabilityId resourceAvailabilityId = ResourceAvailabilityId.newOne();
        ResourceId resourceId = ResourceId.newOne();
        ResourceAvailability resourceAvailability = new ResourceAvailability(resourceAvailabilityId, resourceId, ONE_MONTH);

        //when
        resourceAvailabilityRepository.saveNew(resourceAvailability);

        //then
        ResourceAvailability loaded = resourceAvailabilityRepository.loadById(resourceAvailability.id());
        assertEquals(resourceAvailability, loaded);
        assertEquals(resourceAvailability.segment(), loaded.segment());
        assertEquals(resourceAvailability.resourceId(), loaded.resourceId());
        assertEquals(resourceAvailability.blockedBy(), loaded.blockedBy());
    }
}