package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.shared.capability.Capability;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import org.hibernate.annotations.Type;

import java.util.Set;

@Entity(name = "devices")
class Device {

    @EmbeddedId
    private DeviceId id = DeviceId.newOne();

    @Version
    private int version;

    private String model;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Capability> capabilities;

    String model() {
        return model;
    }


    Set<Capability> capabilities() {
        return capabilities;
    }

    Device(DeviceId id, String model, Set<Capability> capabilities) {
        this.id = id;
        this.model = model;
        this.capabilities = capabilities;
    }

    public Device() {
    }

    public DeviceId id() {
        return id;
    }

}