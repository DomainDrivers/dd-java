package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.List;
import java.util.Set;

public class DeviceFacade {
    private final DeviceRepository deviceRepository;

    public DeviceFacade(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceSummary findDevice(DeviceId deviceId) {
        return deviceRepository.findSummary(deviceId);
    }

    public List<Capability> findAllCapabilities() {
        return deviceRepository.findAllCapabilities();
    }

    public DeviceId createDevice(String model, Set<Capability> assets) {
        DeviceId deviceId = DeviceId.newOne();
        Device device = new Device(deviceId, model, assets);
        return deviceRepository.save(device).id();
    }

}
