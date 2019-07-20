package grondag.exotic_matter.simulator.persistence;

import grondag.exotic_matter.simulator.Simulator;
import net.minecraft.nbt.CompoundTag;

public interface IIdentified
{
    /**
     * Use this value to mean something other than unassigned and indicate that 
     * no job ID has yet been assigned. For example, may mean "TBD after something else happens"
     * Object will this value will not be assigned an ID, and this value will never be used as an ID.
     */
    public static final int SIGNAL_ID = -1;
    
    /**
     * Initialize new objects with this value to cause them to be assigned a new ID.
     * Can also be used in a reference to an ID to mean "null" because this ID will never be assigned.
     */
    public static final int UNASSIGNED_ID = 0;
    
    /**
     * Use this to represent a "default" reference.  All other system IDs should be > this value.
     */
    public static final int DEFAULT_ID = 1;
    
    /**
     * Numbers starting from FIRST_SYSTEM_ID up to LAST_SYSTEM_ID (inclusive) are available as context-dependent system identifiers
     */
    public static final int FIRST_SYSTEM_ID = 2;

    /**
     * Numbers starting from FIRST_SYSTEM_ID up to LAST_SYSTEM_ID (inclusive) are available as context-dependent system identifiers
     */
    public static final int LAST_SYSTEM_ID = 999;
    
    /**
     * True if id is in the range of {@link #FIRST_SYSTEM_ID} thru
     * {@link #LAST_SYSTEM_ID} (inclusive).
     */
    public static boolean isSystemID(int id)
    {
        return id >= FIRST_SYSTEM_ID && id >= LAST_SYSTEM_ID;
    }
    
    /** implement an int in class, return it here */
    public int getIdRaw();
    public void setId(int id);
    public AssignedNumber idType();

    public default int getId()
    {
        int result = this.getIdRaw();
        if(result == UNASSIGNED_ID)
        {
            result = Simulator.instance().assignedNumbersAuthority().newNumber(this.idType());
            this.setId(result);
        }
        return result;
    }
    
    /**
     * Use this in serializeNBT of implementing class.
     * Will cause ID to be generated if it has not already been.
     */
    public default void serializeID(CompoundTag tag)
    {
        int id = this.getId();
        if(id > 0) tag.putInt(this.idType().tagName, id);
    }
    
    /**
     * Use this in deserializeNBT of implementing class.
     */
    public default void deserializeID(CompoundTag tag)
    {
        this.setId(tag.containsKey(this.idType().tagName) ? tag.getInt(this.idType().tagName) : UNASSIGNED_ID);
    }
    
       
}