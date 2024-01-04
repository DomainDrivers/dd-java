package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.util.List;

import static domaindrivers.smartschedule.shared.CapabilitySelector.canPerformAllAtTheTime;

class ScheduleDeviceCapabilities {
    private final DeviceRepository deviceRepository;
    private final CapabilityScheduler capabilityScheduler;

    ScheduleDeviceCapabilities(DeviceRepository deviceRepository, CapabilityScheduler capabilityScheduler) {
        this.deviceRepository = deviceRepository;
        this.capabilityScheduler = capabilityScheduler;
    }

    public List<AllocatableCapabilityId> setupDeviceCapabilities(DeviceId deviceId, TimeSlot timeSlot) {
        DeviceSummary summary = deviceRepository.findSummary(deviceId);
        return capabilityScheduler.scheduleResourceCapabilitiesForPeriod(deviceId.toAllocatableResourceId(),
                List.of(canPerformAllAtTheTime(summary.assets())), timeSlot);
    }
}
