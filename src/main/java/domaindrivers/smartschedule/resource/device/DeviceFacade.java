package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;
import java.util.Set;

public class DeviceFacade {
    private final DeviceRepository deviceRepository;
    private final ScheduleDeviceCapabilities scheduleDeviceCapabilities;

    public DeviceFacade(DeviceRepository deviceRepository, ScheduleDeviceCapabilities scheduleDeviceCapabilities) {
        this.deviceRepository = deviceRepository;
        this.scheduleDeviceCapabilities = scheduleDeviceCapabilities;
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

    public List<AllocatableCapabilityId> scheduleCapabilities(DeviceId deviceId, TimeSlot oneDay) {
        return scheduleDeviceCapabilities.setupDeviceCapabilities(deviceId, oneDay);
    }


}
