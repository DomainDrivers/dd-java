package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendar;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-capability-scheduling.sql", "classpath:schema-availability.sql", "classpath:schema-allocations.sql"})
class CapabilitySchedulingTest {

    @Autowired
    CapabilityScheduler capabilityScheduler;

    @Autowired
    CapabilityFinder capabilityFinder;

    @Autowired
    AvailabilityFacade availabilityFacade;

    @Test
    void canScheduleAllocatableCapabilities() {
        //given
        CapabilitySelector javaSkill = CapabilitySelector.canJustPerform(Capability.skill("JAVA"));
        CapabilitySelector rustSkill = CapabilitySelector.canJustPerform(Capability.skill("RUST"));
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);

        //when
        List<AllocatableCapabilityId> allocatable = capabilityScheduler.scheduleResourceCapabilitiesForPeriod(AllocatableResourceId.newOne(), List.of(javaSkill, rustSkill), oneDay);

        //then
        AllocatableCapabilitiesSummary loaded = capabilityFinder.findById(allocatable);
        assertEquals(allocatable.size(), loaded.all().size());

        assertThat(loaded.all())
                .allMatch(allocatableCapability -> availabilitySlotsAreCreated(allocatableCapability, oneDay));
    }

    boolean availabilitySlotsAreCreated(AllocatableCapabilitySummary allocatableCapability, TimeSlot oneDay) {
        Calendar calendar = availabilityFacade.loadCalendar(allocatableCapability.id().toAvailabilityResourceId(), oneDay);
        return calendar.availableSlots().equals(List.of(oneDay));
    }

    @Test
    void capabilityIsFoundWhenCapabilityPresentInTimeSlot() {
        //given
        Capability fitnessClass = Capability.permission("FITNESS-CLASS");
        CapabilitySelector uniqueSkill = CapabilitySelector.canJustPerform(fitnessClass);
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        TimeSlot anotherDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 2);
        //and
        capabilityScheduler.scheduleResourceCapabilitiesForPeriod(AllocatableResourceId.newOne(), List.of(uniqueSkill), oneDay);

        //when
        AllocatableCapabilitiesSummary found = capabilityFinder.findAvailableCapabilities(fitnessClass, oneDay);
        AllocatableCapabilitiesSummary notFound = capabilityFinder.findAvailableCapabilities(fitnessClass, anotherDay);

        //then
        assertThat(found.all()).hasSize(1);
        assertThat(notFound.all()).isEmpty();
        assertEquals(found.all().get(0).capabilities(), uniqueSkill);
        assertEquals(found.all().get(0).timeSlot(), oneDay);
    }

    @Test
    void capabilityNotFoundWhenCapabilityNotPresent() {
        //given
        CapabilitySelector admin = CapabilitySelector.canJustPerform(Capability.permission("ADMIN"));
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        //and
        capabilityScheduler.scheduleResourceCapabilitiesForPeriod(AllocatableResourceId.newOne(), List.of(admin), oneDay);

        //when
        Capability rustSkill = Capability.skill("RUST JUST FOR NINJAS");
        CapabilitySelector rust = CapabilitySelector.canJustPerform(rustSkill);
        AllocatableCapabilitiesSummary found = capabilityFinder.findCapabilities(rustSkill, oneDay);

        //then
        assertThat(found.all()).isEmpty();
    }

    @Test
    void canScheduleMultipleCapabilitiesOfSameType() {
        //given
        Capability loading = Capability.skill("LOADING_TRUCK");
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 1, 1);
        //and
        AllocatableResourceId truck1 = AllocatableResourceId.newOne();
        AllocatableResourceId truck2 = AllocatableResourceId.newOne();
        AllocatableResourceId truck3 = AllocatableResourceId.newOne();
        capabilityScheduler.scheduleMultipleResourcesForPeriod(Set.of(truck1, truck2, truck3), loading, oneDay);

        //when
        AllocatableCapabilitiesSummary found = capabilityFinder.findCapabilities(loading, oneDay);

        //then
        assertThat(found.all()).hasSize(3);
    }

    @Test
    void canFindCapabilityIgnoringAvailability() {
        //given
        Capability adminPermission = Capability.permission("REALLY_UNIQUE_ADMIN");
        CapabilitySelector admin = CapabilitySelector.canJustPerform(adminPermission);
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(1111, 1, 1);
        TimeSlot differentDay = TimeSlot.createDailyTimeSlotAtUTC(2021, 2, 1);
        TimeSlot hourWithinDay = new TimeSlot(oneDay.from(), oneDay.from().plusSeconds(3600));
        TimeSlot partiallyOverlappingDay = new TimeSlot(oneDay.from().plusSeconds(3600), oneDay.to().plusSeconds(3600));
        //and
        capabilityScheduler.scheduleResourceCapabilitiesForPeriod(AllocatableResourceId.newOne(), List.of(admin), oneDay);

        //when
        AllocatableCapabilitiesSummary onTheExactDay = capabilityFinder.findCapabilities(adminPermission, oneDay);
        AllocatableCapabilitiesSummary onDifferentDay = capabilityFinder.findCapabilities(adminPermission, differentDay);
        AllocatableCapabilitiesSummary inSlotWithin = capabilityFinder.findCapabilities(adminPermission, hourWithinDay);
        AllocatableCapabilitiesSummary inOverlappingSlot = capabilityFinder.findCapabilities(adminPermission, partiallyOverlappingDay);

        //then
        assertThat(onTheExactDay.all()).hasSize(1);
        assertThat(inSlotWithin.all()).hasSize(1);
        assertThat(onDifferentDay.all()).isEmpty();
        assertThat(inOverlappingSlot.all()).isEmpty();
    }



    @Test
    void findingTakesIntoAccountSimulationsCapabilities() {
        //given
        Set<Capability> truckAssets = Set.of(Capability.asset("LOADING"), Capability.asset("CARRYING"));
        CapabilitySelector truckCapabilities = CapabilitySelector.canPerformAllAtTheTime(truckAssets);
        TimeSlot oneDay = TimeSlot.createDailyTimeSlotAtUTC(1111, 1, 1);
        //and
        AllocatableResourceId truckResourceId = AllocatableResourceId.newOne();
        capabilityScheduler.scheduleResourceCapabilitiesForPeriod(truckResourceId, List.of(truckCapabilities), oneDay);

        //when
        AllocatableCapabilityId canPerformBoth = capabilityScheduler.findResourceCapabilities(truckResourceId, truckAssets, oneDay);
        AllocatableCapabilityId canPerformJustLoading = capabilityScheduler.findResourceCapabilities(truckResourceId, Capability.asset("CARRYING"), oneDay);
        AllocatableCapabilityId canPerformJustCarrying = capabilityScheduler.findResourceCapabilities(truckResourceId, Capability.asset("LOADING"), oneDay);
        AllocatableCapabilityId cantPerformJava = capabilityScheduler.findResourceCapabilities(truckResourceId, Capability.skill("JAVA"), oneDay);

        //then
        assertThat(canPerformBoth).isNotNull();
        assertThat(canPerformJustLoading).isNotNull();
        assertThat(canPerformJustCarrying).isNotNull();
        assertThat(cantPerformJava).isNull();
    }

}