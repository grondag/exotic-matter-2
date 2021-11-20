/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.mesh;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fermion.intstream.IntStream;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.Polygon;

@Internal
abstract class AbstractXmMesh implements XmMesh {
	protected IntStream stream;

	/**
	 * Address in stream where poly info starts. Some streams with additional
	 * metadata may start at something other than zero.
	 */
	protected int originAddress;

	/**
	 * Address where next append will occur. Equivalently, shows how many ints have
	 * been written after {@link #originAddress}.
	 */
	protected int writeAddress;

	protected final StreamBackedPolygon reader = new StreamBackedPolygon();
	protected final StreamBackedPolygon polyA = new StreamBackedPolygon();
	protected final StreamBackedPolygon polyB = new StreamBackedPolygon();
	protected final StreamBackedMutablePolygon internal = new StreamBackedMutablePolygon();

	protected AbstractXmMesh() {
		reader.mesh = this;
		polyA.mesh = this;
		polyB.mesh = this;
		internal.mesh = this;
	}

	/**
	 * Value used to initialize origin and writer for new streams and on reset.
	 * Override if stream needs to pack metadatq at front.
	 */
	protected int newOrigin() {
		return 0;
	}

	protected final boolean isValidAddress(int address) {
		return address >= originAddress && address < writeAddress;
	}

	protected final void validateAddress(int address) {
		if (!isValidAddress(address)) {
			throw new IndexOutOfBoundsException(Integer.toString(address));
		}
	}

	@Override
	public final boolean isEmpty() {
		return writeAddress == originAddress;
	}

	@Override
	public final Polygon reader() {
		return reader;
	}

	protected boolean moveReaderToNext(StreamBackedPolygon targetReader) {
		int currentAddress = targetReader.baseAddress;

		if (currentAddress >= writeAddress || currentAddress == EncoderFunctions.BAD_ADDRESS) {
			return false;
		}

		currentAddress += targetReader.stride();

		if (currentAddress >= writeAddress) {
			return false;
		}

		targetReader.moveTo(currentAddress);

		while (targetReader.isDeleted() && currentAddress < writeAddress) {
			currentAddress += targetReader.stride();
			targetReader.moveTo(currentAddress);
		}

		return currentAddress < writeAddress;
	}

	@Override
	public Polygon reader(int address) {
		validateAddress(address);
		reader.moveTo(address);
		return reader;
	}

	void prepare(IntStream stream) {
		didRelease.set(false);
		this.stream = stream;
		originAddress = newOrigin();
		reader.stream = stream;
		polyA.stream = stream;
		polyB.stream = stream;
		internal.stream = stream;
		writeAddress = originAddress;

		// force error on read
		reader.invalidate();
		polyA.invalidate();
		polyB.invalidate();
		internal.invalidate();
	}

	@Override
	public final <T> T release() {
		if (didRelease.compareAndSet(false, true) && readerCount.get() == 0) {
			doRelease();
			returnToPool();
		}

		return null;
	}

	/**
	 * Called after {@link #doRelease()} to return this instance to allocation pool.
	 * Not part of {@link #doRelease()} to allow call of super.doRelease.
	 */
	protected abstract void returnToPool();

	/**
	 * Will be called after release is called and no more concurrent readers are
	 * active.
	 */
	protected void doRelease() {
		reader.invalidate();
		reader.stream = null;
		polyA.stream = null;
		polyB.stream = null;
		internal.stream = null;
		stream.release();
		stream = null;
	}

	@Override
	public Polygon polyA() {
		return polyA;
	}

	@Override
	public Polygon polyA(int address) {
		validateAddress(address);
		polyA.moveTo(address);
		return polyA;
	}

	@Override
	public Polygon polyB() {
		return polyB;
	}

	@Override
	public Polygon polyB(int address) {
		validateAddress(address);
		polyB.moveTo(address);
		return polyB;
	}

	//TODO: remove?
	protected boolean moveReaderToNextLink(StreamBackedPolygon targetReader) {
		int currentAddress = targetReader.baseAddress;

		if (currentAddress >= writeAddress || currentAddress == EncoderFunctions.BAD_ADDRESS) {
			return false;
		}

		int nextAddress = targetReader.link();

		if (nextAddress == Polygon.NO_LINK_OR_TAG || nextAddress >= writeAddress) {
			return false;
		}

		targetReader.moveTo(nextAddress);
		currentAddress = nextAddress;

		while (currentAddress < writeAddress && targetReader.isDeleted()) {
			nextAddress = targetReader.link();

			if (nextAddress == Polygon.NO_LINK_OR_TAG || nextAddress >= writeAddress) {
				return false;
			}

			targetReader.moveTo(nextAddress);
			currentAddress = nextAddress;
		}

		return currentAddress < writeAddress && !targetReader.isDeleted();
	}

	protected void appendCopy(Polygon polyIn, int withFormat) {
		final boolean needReaderLoad = reader.baseAddress == writeAddress;
		final int newFormat = MeshFormat.minimalFixedFormat(polyIn, withFormat);
		stream.set(writeAddress, newFormat);
		internal.moveTo(writeAddress);
		internal.copyFrom(polyIn, true);

		writeAddress += MeshFormat.polyStride(newFormat, true);

		if (needReaderLoad) {
			reader.loadFormat();
		}
	}

	private static class ThreadSafeReader extends StreamBackedPolygon {
		@Override
		public final void release() {
			super.release();

			if (mesh.readerCount.decrementAndGet() == 0 && mesh.didRelease.get()) {
				mesh.doRelease();
			}

			stream = null;
			mesh = null;
			safeReaders.offer(this);
		}
	}

	private static final ArrayBlockingQueue<ThreadSafeReader> safeReaders = new ArrayBlockingQueue<>(256);

	/**
	 * True once our release method has been called. Reset on prepare.
	 */
	protected final AtomicBoolean didRelease = new AtomicBoolean();
	protected final AtomicInteger readerCount = new AtomicInteger();

	/**
	 * Should only be exposed for streams that are immutable.
	 */
	protected Polygon threadSafeReaderImpl() {
		readerCount.incrementAndGet();

		if (didRelease.get()) {
			readerCount.decrementAndGet();
			throw new UnsupportedOperationException("Cannot claim threadsafe reader on released stream.");
		}

		ThreadSafeReader reader = safeReaders.poll();

		if (reader == null) {
			reader = new ThreadSafeReader();
		}

		reader.mesh = this;
		reader.stream = stream;
		reader.moveTo(originAddress);
		return reader;
	}
}
