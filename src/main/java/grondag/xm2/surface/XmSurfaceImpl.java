package grondag.xm2.surface;

import grondag.fermion.structures.SimpleUnorderedArrayList;
import grondag.xm2.api.surface.XmSurface;
import grondag.xm2.api.surface.XmSurfaceList;
import grondag.xm2.api.surface.XmSurfaceListBuilder;
import grondag.xm2.painting.SurfaceTopology;

public class XmSurfaceImpl implements XmSurface {
	public final int ordinal;
	public final String nameKey;
	public final SurfaceTopology topology;
	public final int flags;
	
	private XmSurfaceImpl(int ordinal, String nameKey, SurfaceTopology topology, int flags) {
		this.ordinal = ordinal;
		this.nameKey = nameKey;
		this.topology = topology;
		this.flags = flags;
	}
	
	@Override
	public int ordinal() {
		return ordinal;
	}

	@Override
	public String nameKey() {
		return nameKey;
	}

	@Override
	public SurfaceTopology topology() {
		return topology;
	}

	@Override
	public int flags() {
		return flags;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements XmSurfaceListBuilder {
		private final SimpleUnorderedArrayList<XmSurfaceImpl> surfaces = new SimpleUnorderedArrayList<>();
		private Builder() {}
		
		@Override
		public Builder add(String nameKey, SurfaceTopology topology, int flags) {
			surfaces.add(new XmSurfaceImpl(surfaces.size(), nameKey, topology, flags));
			return this;
		}

		@Override
		public XmSurfaceListImpl build() {
			final int size = surfaces.size();
			XmSurfaceImpl[] output = new XmSurfaceImpl[size];
			for(int i = 0; i < size; i++) {
				output[i] = surfaces.get(i);
			}
			surfaces.clear();
			return new XmSurfaceListImpl(output);
		}
	}
	
	public static class XmSurfaceListImpl implements XmSurfaceList {
		private final int size;
		private final XmSurfaceImpl[] surfaces;
		
		private XmSurfaceListImpl(XmSurfaceImpl[] surfaces) {
			this.surfaces = surfaces;
			this.size = surfaces.length;
		}

		@Override
		public XmSurfaceImpl get(int index) {
			return surfaces[index];
		}

		@Override
		public int size() {
			return size;
		}
	}
}
