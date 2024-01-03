package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {TestDbConfiguration.class, AvailabilityConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
class ResourceAvailabilityOptimisticLockingTest {

    static final TimeSlot ONE_MONTH = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

    @Autowired
    JdbcTemplate jdbcTemplate;

    ResourceAvailabilityRepository resourceAvailabilityRepository;

    @BeforeEach
    void setup() {
        resourceAvailabilityRepository = new ResourceAvailabilityRepository(jdbcTemplate);
    }

    @Test
    void updateBumpsVersion() {
        //given
        ResourceAvailabilityId resourceAvailabilityId = ResourceAvailabilityId.newOne();
        ResourceId resourceId = ResourceId.newOne();
        ResourceAvailability resourceAvailability = new ResourceAvailability(resourceAvailabilityId, resourceId, ONE_MONTH);
        resourceAvailabilityRepository.saveNew(resourceAvailability);

        //when
        resourceAvailability = resourceAvailabilityRepository.loadById(resourceAvailabilityId);
        resourceAvailability.block(Owner.newOne());
        resourceAvailabilityRepository.saveCheckingVersion(resourceAvailability);

        //then
        assertEquals(1, resourceAvailabilityRepository.loadById(resourceAvailabilityId).version());
    }

    @Test
    void cantUpdateConcurrently() throws InterruptedException {
        //given
        ResourceAvailabilityId resourceAvailabilityId = ResourceAvailabilityId.newOne();
        ResourceId resourceId = ResourceId.newOne();
        ResourceAvailability resourceAvailability = new ResourceAvailability(resourceAvailabilityId, resourceId, ONE_MONTH);
        resourceAvailabilityRepository.saveNew(resourceAvailability);
        List<Boolean> results = new ArrayList<>();
        //when
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 1; i<10; i++) {
            executor.execute(()-> {
                try {
                    ResourceAvailability loaded = resourceAvailabilityRepository.loadById(resourceAvailabilityId);
                    loaded.block(Owner.newOne());
                    results.add(resourceAvailabilityRepository.saveCheckingVersion(loaded));
                } catch (Exception e) {
                    // ignore
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        //then
        assertTrue(results.contains(false));
        assertTrue(resourceAvailabilityRepository.loadById(resourceAvailabilityId).version() < 10);
    }

}