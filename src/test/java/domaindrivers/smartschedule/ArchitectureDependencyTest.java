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
                .layer("parallelization").definedBy("domaindrivers.smartschedule.planning.parallelization..")
                .layer("sorter").definedBy("domaindrivers.smartschedule.sorter..")
                .layer("simulation").definedBy("domaindrivers.smartschedule.simulation..")
                .layer("optimization").definedBy("domaindrivers.smartschedule.optimization..")
                .layer("shared").definedBy("domaindrivers.smartschedule.shared..")
                .whereLayer("availability").mayOnlyAccessLayers("shared")
                .whereLayer("parallelization").mayOnlyAccessLayers("sorter", "shared")
                .whereLayer("sorter").mayNotAccessAnyLayer()
                .whereLayer("simulation").mayOnlyAccessLayers("optimization", "shared")
                .whereLayer("optimization").mayOnlyAccessLayers("shared")
                .whereLayer("shared").mayNotAccessAnyLayer()
                .check(classes);
    }
}
