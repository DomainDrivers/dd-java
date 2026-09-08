package domaindrivers.smartschedule.allocation;

sealed interface ProjectAllocationsCommand permits Allocate, Release, DefineSlot, AddDemands {
}
