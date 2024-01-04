package domaindrivers.smartschedule.resource.device;


import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableResourceId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Embeddable
public class DeviceId implements Serializable {

    public static DeviceId newOne() {
        return new DeviceId(UUID.randomUUID());
    }

    private UUID deviceId;

    DeviceId(UUID uuid) {
        this.deviceId = uuid;
    }

    public DeviceId() {
    }

    public UUID id() {
        return deviceId;
    }

    public AllocatableResourceId toAllocatableResourceId() {
        return new AllocatableResourceId(deviceId);
    }
}