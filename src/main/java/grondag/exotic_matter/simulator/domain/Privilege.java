package grondag.exotic_matter.simulator.domain;

import grondag.fermion.structures.BinaryEnumSet;

public enum Privilege
{
    ADMIN,
    REMOVE_NODE,
    ADD_NODE,
    ACCESS_INVENTORY,
    CONSTRUCTION_VIEW,
    CONSTRUCTION_EDIT;

    public static final BinaryEnumSet<Privilege> PRIVILEGE_FLAG_SET = new BinaryEnumSet<Privilege>(Privilege.class);
}