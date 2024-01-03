package domaindrivers.smartschedule.resource;

import domaindrivers.smartschedule.resource.device.DeviceFacade;
import domaindrivers.smartschedule.resource.employee.EmployeeFacade;
import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.List;
import java.util.stream.Stream;

public class ResourceFacade {

    private final EmployeeFacade employeeFacade;
    private final DeviceFacade deviceFacade;

    ResourceFacade(EmployeeFacade employeeFacade, DeviceFacade deviceFacade) {
        this.employeeFacade = employeeFacade;
        this.deviceFacade = deviceFacade;
    }

    public List<Capability> findAllCapabilities() {
        List<Capability> employeeCapabilities = employeeFacade.findAllCapabilities();
        List<Capability> deviceCapabilities = deviceFacade.findAllCapabilities();
        return Stream.concat(employeeCapabilities.stream(), deviceCapabilities.stream()).toList();
    }
}
