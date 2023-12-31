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
                .layer("parallelization").definedBy("domaindrivers.smartschedule.planning.parallelization..")
                .layer("sorter").definedBy("domaindrivers.smartschedule.sorter..")
                .whereLayer("parallelization").mayOnlyAccessLayers("sorter")
                .whereLayer("sorter").mayNotAccessAnyLayer()
                .check(classes);
    }
}
