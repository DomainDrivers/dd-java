package domaindrivers.smartschedule.availability;

record Blockade(Owner takenBy, boolean disabled) {

    static Blockade none() {
        return new Blockade(Owner.none(), false);
    }

    public static Blockade disabledBy(Owner owner) {
        return new Blockade(owner, true);
    }

    public static Blockade ownedBy(Owner owner) {
        return new Blockade(owner, false);
    }

    boolean canBeTakenBy(Owner requester) {
        return takenBy.byNone() || takenBy.equals(requester);
    }

     boolean isDisabledBy(Owner owner) {
        return disabled && owner.equals(this.takenBy);
    }
}
