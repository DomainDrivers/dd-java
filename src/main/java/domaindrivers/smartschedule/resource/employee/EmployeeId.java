package domaindrivers.smartschedule.resource.employee;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class EmployeeId implements Serializable {

    public static EmployeeId newOne() {
        return new EmployeeId(UUID.randomUUID());
    }

    private UUID employeeId;

    EmployeeId(UUID uuid) {
        this.employeeId = uuid;
    }

    public EmployeeId() {
    }

    public UUID id() {
        return employeeId;
    }

}

