package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {TestDbConfiguration.class, AvailabilityConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
class ResourceAvailabilityUniquenessTest {

    static final TimeSlot ONE_MONTH = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

    @Autowired
    JdbcTemplate jdbcTemplate;

    ResourceAvailabilityRepository resourceAvailabilityRepository;

    @BeforeEach
    void setup() {
        resourceAvailabilityRepository = new ResourceAvailabilityRepository(jdbcTemplate);
    }

    @Test
    void cantSaveTwoAvailabilitiesWithSameResourceIdAndSegment() {
        //given
        ResourceAvailabilityId resourceId = ResourceAvailabilityId.newOne();
        ResourceAvailabilityId anotherResourceId = ResourceAvailabilityId.newOne();
        ResourceAvailabilityId resourceAvailabilityId = ResourceAvailabilityId.newOne();

        //when
        resourceAvailabilityRepository.saveNew(new ResourceAvailability(resourceAvailabilityId, resourceId, ONE_MONTH));

        //expect
        assertThrows(DuplicateKeyException.class, () -> resourceAvailabilityRepository.saveNew(new ResourceAvailability(resourceAvailabilityId, anotherResourceId, ONE_MONTH)));
    }



}