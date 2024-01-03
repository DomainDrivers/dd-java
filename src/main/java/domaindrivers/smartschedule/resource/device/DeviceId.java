package domaindrivers.smartschedule.resource.device;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
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
}