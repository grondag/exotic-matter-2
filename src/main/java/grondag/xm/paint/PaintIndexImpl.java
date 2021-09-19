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

import java.util.Arrays;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.network.PaintIndexSnapshotS2C;
import grondag.xm.network.PaintIndexUpdateS2C;
import grondag.xm.paint.XmPaintImpl.Finder;

public class PaintIndexImpl implements PaintIndex {
	public static final PaintIndexImpl CLIENT = new PaintIndexImpl(true);
	public static final PaintIndexImpl SERVER = new PaintIndexImpl(false);

	private int nextIndex = 0;
	private int capacity  = 1024;
	private XmPaint[] paints = new XmPaint[capacity];
	private boolean isDirty = true;
	private PlayerList playerManager = null;
	public final boolean isClient;

	private PaintIndexImpl(boolean isClient) {
		this.isClient = isClient;
	}

	public void clear() {
		nextIndex = 0;
		Arrays.fill(paints, null);
		isDirty = true;
		playerManager = null;
	}

	private void ensureCapacity(int index) {
		if (index >= capacity) {
			final int newCapacity = Mth.smallestEncompassingPowerOfTwo(index);
			final XmPaint[] newPaints = new XmPaint[newCapacity];
			System.arraycopy(paints, 0, newPaints, 0, nextIndex);
			paints = newPaints;
			capacity = newCapacity;
		}
	}

	@Override
	public XmPaint fromIndex(int index) {
		assert index >= 0;
		assert index < nextIndex;
		return index >= 0 && index < nextIndex ? paints[index] : XmPaintImpl.DEFAULT_PAINT;
	}

	@Override
	public XmPaint index(XmPaint paint) {
		if (isClient) {
			throw new UnsupportedOperationException("XmPaint index cannot be created on logical client.");
		}

		final XmPaintImpl.Finder finder = (Finder) XmPaintImpl.finder().copy(paint);
		final int index;

		synchronized (this) {
			index = nextIndex++;
			ensureCapacity(index);
			paint = finder.index(index).find();
			paints[index] = paint;
			isDirty = true;
		}

		sendToListeners(paint, index);
		return paint;
	}

	@Override
	public void updateIndex(int index, XmPaint paint) {
		if (paint == null) {
			paint = XmPaintImpl.DEFAULT_PAINT;
		}

		((XmPaintImpl) paints[index]).copyFrom((XmPaintImpl) paint);
		isDirty = true;
		sendToListeners(paint, index);
	}

	private void sendToListeners(XmPaint paint, int index) {
		playerManager.broadcastAll(PaintIndexUpdateS2C.toPacket(paint, index));
	}

	public void save() {
		if (isDirty) {
			// TODO:
			isDirty = false;
		}
	}

	public ListTag toTag() {
		assert !isClient;
		final int limit = nextIndex;
		final ListTag tag = new ListTag();

		for (int i = 0; i < limit; ++i)  {
			tag.add(paints[i].toFixedTag());
		}

		return tag;
	}

	public void fromTag(ListTag tag, ServerLevel world) {
		assert !isClient;
		clear();

		final int limit = tag.size();
		ensureCapacity(limit);
		nextIndex = limit;

		for (int i = 0; i < limit; ++i)  {
			paints[i] = XmPaint.fromTag(tag.getCompound(i), null);
		}
	}

	public void connectPlayer(ServerPlayer player) {
		final PlayerList playerManager = player.server.getPlayerList();

		if (this.playerManager == null) {
			this.playerManager = playerManager;
		} else {
			assert playerManager == this.playerManager;
		}

		player.connection.send(PaintIndexSnapshotS2C.toPacket(this));
	}

	public void toBytes(FriendlyByteBuf pBuff) {
		assert !isClient;

		final int limit = nextIndex;
		pBuff.writeVarInt(limit);

		for (int i = 0; i < limit; ++i)  {
			paints[i].toBytes(pBuff);
		}
	}

	public void fromArray(XmPaint[] paints) {
		assert isClient;

		clear();
		final int limit = paints.length;
		ensureCapacity(limit);
		nextIndex = limit;
		System.arraycopy(paints, 0, this.paints, 0, limit);
	}

	public static XmPaint[] arrayFromBytes(FriendlyByteBuf pBuff) {
		final int limit = pBuff.readVarInt();
		final XmPaint[] result = new XmPaint[limit];

		for (int i = 0; i < limit; ++i)  {
			result[i] = XmPaint.fromBytes(pBuff, null);
		}

		return result;
	}

	public void updateClientIndex(XmPaint paint, int index) {
		ensureCapacity(index);
		paints[index] = paint;
	}
}
