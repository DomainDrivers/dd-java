package domaindrivers.smartschedule.resource.device;

import domaindrivers.smartschedule.shared.capability.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

interface DeviceRepository extends JpaRepository<Device, DeviceId> {

    default DeviceSummary findSummary(DeviceId deviceId) {
        Device device = this.findById(deviceId).orElseThrow();
        Set<Capability> assets = device.capabilities();
        return new DeviceSummary(deviceId, device.model(), assets);
    }

    default List<Capability> findAllCapabilities() {
        return this.findAll().stream().flatMap(device -> device.capabilities().stream()).collect(Collectors.toList());
    }
}