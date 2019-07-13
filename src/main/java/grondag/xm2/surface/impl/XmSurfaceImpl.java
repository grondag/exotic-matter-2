package grondag.xm2.surface.impl;

import grondag.fermion.structures.SimpleUnorderedArrayList;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.surface.api.XmSurface;
import grondag.xm2.surface.api.XmSurfaceList;
import grondag.xm2.surface.api.XmSurfaceListBuilder;

public class XmSurfaceImpl implements XmSurface {
	public final int ordinal;
	public final String nameKey;
	public final SurfaceTopology topology;
	public final float uvWrapDistance;
	public final int flags;
	
	private XmSurfaceImpl(int ordinal, String nameKey, SurfaceTopology topology, float uvWrapDistance, int flags) {
		this.ordinal = ordinal;
		this.nameKey = nameKey;
		this.topology = topology;
		this.uvWrapDistance = uvWrapDistance;
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
	public float uvWrapDistance() {
		return uvWrapDistance;
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
		public Builder add(String nameKey, SurfaceTopology topology, float uvWrapDistance, int flags) {
			surfaces.add(new XmSurfaceImpl(surfaces.size(), nameKey, topology, uvWrapDistance, flags));
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
