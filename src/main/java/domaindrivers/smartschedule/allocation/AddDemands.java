package domaindrivers.smartschedule.allocation;

import java.time.Instant;

record AddDemands(Demands newDemands, Instant when) implements ProjectAllocationsCommand {
}
