package domaindrivers.smartschedule.shared;

import domaindrivers.smartschedule.shared.capability.Capability;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CapabilitySelectorTest {

    static final Capability RUST = new Capability("RUST", "SKILL");
    static final Capability BEING_AN_ADMIN = new Capability("ADMIN", "PERMISSION");
    static final Capability JAVA = new Capability("JAVA", "SKILL");

    @Test
    void allocatableResourceCanPerformOnlyOneOfPresentCapabilities() {
        //given
        CapabilitySelector adminOrRust = CapabilitySelector.canPerformOneOf(Set.of(BEING_AN_ADMIN, RUST));

        //expect
        assertTrue(adminOrRust.canPerform(BEING_AN_ADMIN));
        assertTrue(adminOrRust.canPerform(RUST));
        assertFalse(adminOrRust.canPerform(Set.of(RUST, BEING_AN_ADMIN)));
        assertFalse(adminOrRust.canPerform(new Capability("JAVA", "SKILL")));
        assertFalse(adminOrRust.canPerform(new Capability("LAWYER", "PERMISSION")));
    }

    @Test
    void allocatableResourceCanPerformSimultaneousCapabilities() {
        //given
        CapabilitySelector adminAndRust = CapabilitySelector.canPerformAllAtTheTime(Set.of(BEING_AN_ADMIN, RUST));

        //expect
        assertTrue(adminAndRust.canPerform(BEING_AN_ADMIN));
        assertTrue(adminAndRust.canPerform(RUST));
        assertTrue(adminAndRust.canPerform(Set.of(RUST, BEING_AN_ADMIN)));
        assertFalse(adminAndRust.canPerform(Set.of(RUST, BEING_AN_ADMIN, JAVA)));
        assertFalse(adminAndRust.canPerform(JAVA));
        assertFalse(adminAndRust.canPerform(new Capability("LAWYER", "PERMISSION")));
    }
}