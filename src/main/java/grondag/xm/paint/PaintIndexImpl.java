/*******************************************************************************
 * Copyright 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.paint;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;

import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.paint.XmPaint;

// TODO: complete implementation
// reset/synchronize on client connect
// send deltas to connected clients
// serialize/load to/from world on server
public class PaintIndexImpl implements PaintIndex {
	private final ObjectArrayList<XmPaint> list = new ObjectArrayList<>();
	private final Object2IntOpenHashMap<XmPaint> map = new Object2IntOpenHashMap<>();
	boolean isClient = false;

	PaintIndexImpl() {
		map.defaultReturnValue(-1);
	}
	public void clear() {
		list.clear();
		map.clear();
	}

	/** set to true on clients - signals that should not be modified except via packets */
	public void setClient(boolean isClient) {
		this.isClient = isClient;
	}

	@Override
	public XmPaint fromInt(int index) {
		assert index < list.size();
		return index < list.size() ? list.get(index) : XmPaintImpl.DEFAULT_PAINT;
	}

	@Override
	public int toInt(XmPaint paint) {
		assert !isClient;

		int result = map.getInt(paint);

		if (result == -1) {
			synchronized (map) {
				result = map.getInt(paint);

				if (result == -1) {
					result = list.size();
					list.add(paint);
					map.put(paint, result);
				}
			}
		}

		return result;
	}

	public ListTag toTag() {
		assert !isClient;
		final int limit = list.size();
		final ListTag tag = new ListTag();

		for (int i = 0; i < limit; ++i)  {
			tag.add(list.get(i).toTag());
		}

		return tag;
	}

	public void fromTag(ListTag tag) {
		assert !isClient;
		clear();

		final int limit = tag.size();

		for (int i = 0; i < limit; ++i)  {
			final XmPaint paint = XmPaint.fromTag(tag.getCompound(i));
			list.add(paint);
			map.put(paint, i);
		}
	}

	public void toBytes(PacketByteBuf pBuff) {
		assert !isClient;

		final int limit = list.size();
		pBuff.writeVarInt(limit);

		for (int i = 0; i < limit; ++i)  {
			list.get(i).toBytes(pBuff);
		}
	}

	public void fromBytes(PacketByteBuf pBuff) {
		assert isClient;

		clear();
		final int limit = pBuff.readVarInt();

		for (int i = 0; i < limit; ++i)  {
			final XmPaint paint = XmPaint.fromBytes(pBuff);
			list.add(paint);
			map.put(paint, i);
		}
	}
}
