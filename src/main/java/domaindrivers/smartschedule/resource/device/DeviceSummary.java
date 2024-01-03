package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.Set;

public record DeviceSummary(DeviceId id, String model, Set<Capability> assets) {
}
