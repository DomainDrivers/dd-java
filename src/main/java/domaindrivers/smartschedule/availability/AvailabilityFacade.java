package domaindrivers.smartschedule.availability;


import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

public class AvailabilityFacade {

    //can start with an in-memory repository for the aggregate

    public void createResourceSlots(ResourceAvailabilityId resourceId, TimeSlot timeslot) {

    }

    public boolean block(ResourceAvailabilityId resourceId, TimeSlot timeSlot, Owner requester) {
        return true;
    }

    public boolean release(ResourceAvailabilityId resourceId, TimeSlot timeSlot, Owner requester) {
        return true;
    }

    public boolean disable(ResourceAvailabilityId resourceId, TimeSlot timeSlot, Owner requester) {
        return true;
    }


}


