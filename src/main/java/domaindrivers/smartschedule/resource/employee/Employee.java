package domaindrivers.smartschedule.resource.employee;


import domaindrivers.smartschedule.shared.capability.Capability;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import org.hibernate.annotations.Type;

import java.util.Set;

@Entity(name = "employees")
class Employee {

    @EmbeddedId
    private EmployeeId id = EmployeeId.newOne();

    @Version
    private int version;

    private String name;

    private String lastName;

    private Seniority seniority;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Capability> capabilities;

    String name() {
        return name;
    }

    String lastName() {
        return lastName;
    }

    Seniority seniority() {
        return seniority;
    }

    Set<Capability> capabilities() {
        return capabilities;
    }

    Employee(EmployeeId id, String name, String lastName, Seniority status, Set<Capability> capabilities) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.seniority = status;
        this.capabilities = capabilities;
    }

    public Employee() {
    }

    public EmployeeId id() {
        return id;
    }

}

