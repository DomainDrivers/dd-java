package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.MockedEventPublisherConfiguration;
import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestDbConfiguration.class, MockedEventPublisherConfiguration.class})
@Sql(scripts = "classpath:schema-availability.sql")
public class TakingRandomResourceTest {

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Test
    void canTakeRandomResourceFromPool() {
        //given
        ResourceId resourceId = ResourceId.newOne();
        ResourceId resourceId2 = ResourceId.newOne();
        ResourceId resourceId3 = ResourceId.newOne();
        Set<ResourceId> resourcesPool = Set.of(resourceId, resourceId2, resourceId3);
        //and
        Owner owner1 = Owner.newOne();
        Owner owner2 = Owner.newOne();
        Owner owner3 = Owner.newOne();
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

        //and
        availabilityFacade.createResourceSlots(resourceId,  oneDay);
        availabilityFacade.createResourceSlots(resourceId2, oneDay);
        availabilityFacade.createResourceSlots(resourceId3, oneDay);

        //when
        Optional<ResourceId> taken1 = availabilityFacade.blockRandomAvailable(resourcesPool, oneDay, owner1);

        //then
        assertThat(taken1).hasValueSatisfying(resourcesPool::contains);
        taken1.ifPresent(value -> assertThatResourceIsTakeByOwner(value, owner1, oneDay));

        //when
        Optional<ResourceId> taken2 = availabilityFacade.blockRandomAvailable(resourcesPool, oneDay, owner2);

        //then
        assertThat(taken2).hasValueSatisfying(resourcesPool::contains);
        taken2.ifPresent(value -> assertThatResourceIsTakeByOwner(value, owner2, oneDay));

        //when
        Optional<ResourceId> taken3 = availabilityFacade.blockRandomAvailable(resourcesPool, oneDay, owner3);

        //then
        assertThat(taken3).hasValueSatisfying(resourcesPool::contains);
        taken3.ifPresent(value -> assertThatResourceIsTakeByOwner(value, owner3, oneDay));

        //when
        Optional<ResourceId> taken4 = availabilityFacade.blockRandomAvailable(resourcesPool, oneDay, owner3);

        //then
        assertThat(taken4).isEmpty();
    }

    @Test
    void nothingIsTakenWhenNoResourceInPool() {
        //given
        Set<ResourceId> resources = Set.of(ResourceId.newOne(), ResourceId.newOne(), ResourceId.newOne());

        //when
        TimeSlot jan_1 = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        Optional<ResourceId> taken1 = availabilityFacade.blockRandomAvailable(resources, jan_1, Owner.newOne());

        //then
        assertThat(taken1).isEmpty();
    }

    void assertThatResourceIsTakeByOwner(ResourceId resourceId, Owner owner, TimeSlot oneDay) {
        ResourceGroupedAvailability resourceAvailability = availabilityFacade.find(resourceId, oneDay);
        assertThat(resourceAvailability.availabilities()).allMatch(ra -> ra.blockedBy().equals(owner));
    }



}