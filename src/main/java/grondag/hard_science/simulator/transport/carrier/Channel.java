package grondag.hard_science.simulator.transport.carrier;

import grondag.hard_science.simulator.resource.StorageType;

/**
 * Channels are integer IDs used to tag transport ports
 * and circuits to constrain connectivity. This class
 * defines constants and utility methods for working
 * with channel values.<p>
 * 
 * Circuits/ports use device species to
 * determine channel, or one of the constants defined below.
 * Different tiers of circuit do NOT need the same channel.
 * For convenience, this logic is codified in 
 * {@link StorageType#channelsSpanLevels()}<p>
 * 
 * By convention, top-level ports/circuits
 * should always used TOP_CHANNEL as their channel so that they pass all
 * tests for channel matching without special handling.
 *
 */
public class Channel
{
    /**
     * Lowest value that will be used for channel.  
     * No special constants should be defined with a value less than this.
     * Needed for serializing port descriptions.
     */
    public static final int MIN_CHANNEL_VALUE = -8;
    
    /**
     * Max value that will be used for channel.
     * Non-top channels currently limited to 0-15 <p>
     * 
     * Note this max value is also the value used to 
     * represent the "top" channel for top-level item/power
     * carrier circuits.
     */
    public static final int MAX_CHANNEL_VALUE = 500;

    
    public static final int TOP_CHANNEL = MAX_CHANNEL_VALUE;
    
    /**
     * Means the port doesn't have a channel yet but could.
     * Port should not connect until a channel is set.
     */
    public static final int CONFIGURABLE_NOT_SET = -1;
    
    /**
     * Means the port should have a channel but doesn't
     * or something is preventing the channel from being recognized.
     * Port should not connect until a channel is set.
     */
    public static final int INVALID_CHANNEL = -2;
    
    /**
     * Means the port will be configured to follow the 
     * device channel when it is created. Intended for use
     * in port layout and should not be encountered in device ports. <p>
     * 
     * Generally only applies only to carrier ports. 
     * Does not apply to the external side of bridge ports.
     */
    public static final int CONFIGURABLE_FOLLOWS_DEVICE = -3;

    /**
     * True if the channel is likely to be a valid value and
     * not one of the symbolic constants defined in this class.
     */
    public static boolean isRealChannel(int channel)
    {
        return channel >= 0;
    }
    
    /**
     * Encapsulates the logic that top-level carriers
     * should always used {@link #TOP_CHANNEL} as their channel.
     * Returns input channel if logic does not apply.
     */
    public static int channelOverride(int channel, CarrierLevel level, StorageType<?> storageType)
    {
        return level.isTop()
                ? TOP_CHANNEL
                : channel;
    }
}
