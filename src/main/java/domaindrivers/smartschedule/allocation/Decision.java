package domaindrivers.smartschedule.allocation;

import java.util.Optional;

record Decision<E>(ProjectAllocations nextState, Optional<E> event) {
}
