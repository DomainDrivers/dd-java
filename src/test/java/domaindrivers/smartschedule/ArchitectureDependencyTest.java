package domaindrivers.smartschedule;


import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;


public class ArchitectureDependencyTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("domaindrivers.smartschedule");

    @Test
    void checkDependencies() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("availability").definedBy("domaindrivers.smartschedule.availability..")
                .layer("allocation").definedBy("domaindrivers.smartschedule.allocation..")
                .layer("capabilityscheduling").definedBy("domaindrivers.smartschedule.allocation.capabilityscheduling..")
                .layer("capabilityscheduling-acl").definedBy("domaindrivers.smartschedule.allocation.capabilityscheduling.legacyacl..")
                .layer("cashflow").definedBy("domaindrivers.smartschedule.allocation.cashflow..")
                .layer("parallelization").definedBy("domaindrivers.smartschedule.planning.parallelization..")
                .layer("sorter").definedBy("domaindrivers.smartschedule.sorter..")
                .layer("simulation").definedBy("domaindrivers.smartschedule.simulation..")
                .layer("optimization").definedBy("domaindrivers.smartschedule.optimization..")
                .layer("resource").definedBy("domaindrivers.smartschedule.resource..")
                .layer("employee").definedBy("domaindrivers.smartschedule.resource.employee..")
                .layer("device").definedBy("domaindrivers.smartschedule.resource.device..")
                .layer("shared").definedBy("domaindrivers.smartschedule.shared..")
                .whereLayer("availability").mayOnlyAccessLayers("shared")
                .whereLayer("allocation").mayOnlyAccessLayers("shared", "availability", "simulation", "optimization", "capabilityscheduling")
                .whereLayer("parallelization").mayOnlyAccessLayers("sorter", "shared", "availability")
                .whereLayer("sorter").mayNotAccessAnyLayer()
                .whereLayer("simulation").mayOnlyAccessLayers("optimization", "shared")
                .whereLayer("optimization").mayOnlyAccessLayers("shared")
                .whereLayer("cashflow").mayOnlyAccessLayers("shared", "allocation")
                .whereLayer("capabilityscheduling").mayOnlyAccessLayers("shared", "availability")
                .whereLayer("employee").mayOnlyAccessLayers("allocation", "shared")
                .whereLayer("device").mayOnlyAccessLayers("allocation", "shared")
                .whereLayer("capabilityscheduling-acl").mayOnlyAccessLayers("shared", "capabilityscheduling")
                .whereLayer("shared").mayNotAccessAnyLayer()
                .check(classes);
    }
}
