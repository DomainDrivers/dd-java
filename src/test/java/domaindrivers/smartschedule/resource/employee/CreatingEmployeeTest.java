package domaindrivers.smartschedule.resource.employee;

import domaindrivers.smartschedule.TestDbConfiguration;
import domaindrivers.smartschedule.shared.capability.Capability;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static domaindrivers.smartschedule.resource.employee.Seniority.SENIOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-resources.sql"})
class CreatingEmployeeTest {

    @Autowired
    EmployeeFacade employeeFacade;

    @Test
    void canCreateAndLoadEmployee() {
        //given
        EmployeeId employee =
                employeeFacade.addEmployee("resourceName", "lastName", SENIOR, Capability.skills("JAVA, PYTHON"), Capability.permissions("ADMIN, COURT"));

        //when
        EmployeeSummary loaded = employeeFacade.findEmployee(employee);

        //then
        assertThat(loaded.skills()).containsOnlyOnceElementsOf(Capability.skills("JAVA, PYTHON"));
        assertEquals(Capability.permissions("ADMIN, COURT"), loaded.permissions());
        assertEquals("resourceName", loaded.name());
        assertEquals("lastName", loaded.lastName());
        assertEquals(SENIOR, loaded.seniority());

    }

    @Test
    void canFindAllCapabilities() {
        //given
       employeeFacade.addEmployee("staszek", "lastName", SENIOR, Capability.skills("JAVA12", "PYTHON21"), Capability.permissions("ADMIN1", "COURT1"));
       employeeFacade.addEmployee("leon", "lastName", SENIOR, Capability.skills("JAVA12", "PYTHON21"), Capability.permissions("ADMIN2", "COURT2"));
       employeeFacade.addEmployee("sławek", "lastName", SENIOR, Capability.skills("JAVA12", "PYTHON21"), Capability.permissions("ADMIN3", "COURT3"));

        //when
        List<Capability> loaded = employeeFacade.findAllCapabilities();

        //then
        assertThat(loaded).contains(
                Capability.permission("ADMIN1"),
                Capability.permission("ADMIN2"),
                Capability.permission("ADMIN3"),
                Capability.permission("COURT1"),
                Capability.permission("COURT2"),
                Capability.permission("COURT3"),
                Capability.skill("JAVA12"),
                Capability.skill("JAVA12"),
                Capability.skill("JAVA12"),
                Capability.skill("PYTHON21"),
                Capability.skill("PYTHON21"),
                Capability.skill("PYTHON21")
        );

    }
}