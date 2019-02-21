package grondag.exotic_matter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("null")
public class PolyLocality {
    private interface IIdealPolygon {

        void addVertex(int i, float x, float y, float z, float u, float v, int color);

        int vertexCount();

        IIdealVertex getVertex(int i);

        int getVertexSum(int i);

    }

    private interface IIdealVertex {
        float x();

        float y();

        float z();

    }

    private class ObjectVertex implements IIdealVertex {
        private final float x;
        private final float y;
        private final float z;

        private ObjectVertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public float x() {
            return this.x;
        }

        @Override
        public float y() {
            return this.y;
        }

        @Override
        public float z() {
            return this.z;
        }
    }

    private class BasicPoly implements IIdealPolygon {

        private final ObjectVertex[] vertices = new ObjectVertex[4];

        @Override
        public void addVertex(int i, float x, float y, float z, float u, float v, int color) {
            this.vertices[i] = new ObjectVertex(x, y, z);
        }

        @Override
        public int vertexCount() {
            return 4;
        }

        @Override
        public IIdealVertex getVertex(int i) {
            return this.vertices[i];
        }

        @Override
        public int getVertexSum(int i) {
            ObjectVertex v = this.vertices[i];
            return (int) (v.x + v.y + v.z);
        }

    }

    private class VertValueArrayPoly implements IIdealPolygon {

        private final float[] vertexData = new float[12];

        @Override
        public void addVertex(int i, float x, float y, float z, float u, float v, int color) {
            final int offset = i * 3;
            this.vertexData[offset] = x;
            this.vertexData[offset + 1] = y;
            this.vertexData[offset + 2] = z;
        }

        @Override
        public int vertexCount() {
            return 4;
        }

        private class VertexFacade implements IIdealVertex {
            private final int offset;

            private VertexFacade(int index) {
                this.offset = index * 3;
            }

            @Override
            public float x() {
                return vertexData[offset];
            }

            @Override
            public float y() {
                return vertexData[offset + 1];
            }

            @Override
            public float z() {
                return vertexData[offset + 2];
            }

        }

        @Override
        public IIdealVertex getVertex(int i) {
            return new VertexFacade(i);
        }

        @Override
        public int getVertexSum(int i) {
            final int offset = i * 3;
            return (int) (this.vertexData[offset] + this.vertexData[offset + 1] + this.vertexData[offset + 2]);
        }

    }

    private class DirectRefPoly implements IIdealPolygon {

        private ObjectVertex v0;
        private ObjectVertex v1;
        private ObjectVertex v2;
        private ObjectVertex v3;

        @Override
        public void addVertex(int i, float x, float y, float z, float u, float v, int color) {
            ObjectVertex newVert = new ObjectVertex(x, y, z);
            switch (i) {
            case 0:
                this.v0 = newVert;
                break;
            case 1:
                this.v1 = newVert;
                break;
            case 2:
                this.v2 = newVert;
                break;
            default:
                this.v3 = newVert;
                break;
            }
        }

        @Override
        public int vertexCount() {
            return 4;
        }

        @Override
        public IIdealVertex getVertex(int i) {
            switch (i) {
            case 0:
                return this.v0;
            case 1:
                return this.v1;
            case 2:
                return this.v2;
            default:
                return this.v3;
            }
        }

        @Override
        public int getVertexSum(int i) {
            ObjectVertex v = null;
            switch (i) {
            case 0:
                v = this.v0;
                break;
            case 1:
                v = this.v1;
                break;
            case 2:
                v = this.v2;
                break;
            case 3:
                v = this.v3;
                break;
            }
            return (int) (v.x + v.y + v.z);
        }
    }

    private class DirectBigInitPoly implements IIdealPolygon {

        private final ObjectVertex v0;
        private final ObjectVertex v1;
        private final ObjectVertex v2;
        private final ObjectVertex v3;

        public DirectBigInitPoly(float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1,
                float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3,
                float u3, float v3, int color) {
            this.v0 = new ObjectVertex(x0, y0, z0);
            this.v1 = new ObjectVertex(x0, y0, z0);
            this.v2 = new ObjectVertex(x0, y0, z0);
            this.v3 = new ObjectVertex(x0, y0, z0);
        }

        @Override
        public int vertexCount() {
            return 4;
        }

        @Override
        public IIdealVertex getVertex(int i) {
            switch (i) {
            case 0:
                return this.v0;
            case 1:
                return this.v1;
            case 2:
                return this.v2;
            default:
                return this.v3;
            }
        }

        @Override
        public int getVertexSum(int i) {
            ObjectVertex v = null;
            switch (i) {
            case 0:
                v = this.v0;
                break;
            case 1:
                v = this.v1;
                break;
            case 2:
                v = this.v2;
                break;
            case 3:
                v = this.v3;
                break;
            }
            return (int) (v.x + v.y + v.z);
        }

        @Override
        public void addVertex(int i, float x, float y, float z, float u, float v, int color) {
            throw new UnsupportedOperationException();
        }
    }

    private interface IPolyMaker {
        public IIdealPolygon makePoly(Random r);

        public default int accessPolyExternal(IIdealPolygon p) {
            int sum = 0;
            final int n = p.vertexCount();
            for (int i = 0; i < n; i++) {
                IIdealVertex v = p.getVertex(i);
                sum += (v.x() + v.y() + v.z());
            }
            return sum;
        }

        public default int accessPolyInternal(IIdealPolygon p) {
            int sum = 0;
            final int n = p.vertexCount();
            for (int i = 0; i < n; i++) {
                sum += p.getVertexSum(i);
            }
            return sum;
        }

        public String label();
    }

    private class BasicMaker implements IPolyMaker {
        @Override
        public IIdealPolygon makePoly(Random r) {
            IIdealPolygon result = new BasicPoly();
            result.addVertex(0, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(1, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(2, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(3, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            return result;
        }

        @Override
        public String label() {
            return "Vertex";
        }
    }

    private class DirectVertValuePoly implements IIdealPolygon {
        private float x0;
        private float y0;
        private float z0;
        // private float u0;
        // private float v0;
        // private int vcolor0;
        // private float normalX0;
        // private float normalY0;
        // private float normalZ0;

        private class V0 implements IIdealVertex {

            @Override
            public float x() {
                return x0;
            }

            @Override
            public float y() {
                return y0;
            }

            @Override
            public float z() {
                return z0;
            }

            // @Override
            // public float u() { return u0; }
            //
            // @Override
            // public float v() { return v0; }
            //
            // @Override
            // public int color() { return vcolor0; }
            //
            // @Override
            // public float normalX() { return normalX0; }
            //
            // @Override
            // public float normalY() { return normalY0; }
            //
            // @Override
            // public float normalZ() { return normalZ0; }

        }

        private float x1;
        private float y1;
        private float z1;
        // private float u1;
        // private float v1;
        // private int vcolor1;
        // private float normalX1;
        // private float normalY1;
        // private float normalZ1;

        private class V1 implements IIdealVertex {

            @Override
            public float x() {
                return x1;
            }

            @Override
            public float y() {
                return y1;
            }

            @Override
            public float z() {
                return z1;
            }

            // @Override
            // public float u() { return u1; }
            //
            // @Override
            // public float v() { return v1; }
            //
            // @Override
            // public int color() { return vcolor1; }
            //
            // @Override
            // public float normalX() { return normalX1; }
            //
            // @Override
            // public float normalY() { return normalY1; }
            //
            // @Override
            // public float normalZ() { return normalZ1; }

        }

        private float x2;
        private float y2;
        private float z2;
        // private float u2;
        // private float v2;
        // private int vcolor2;
        // private float normalX2;
        // private float normalY2;
        // private float normalZ2;

        private class V2 implements IIdealVertex {

            @Override
            public float x() {
                return x2;
            }

            @Override
            public float y() {
                return y2;
            }

            @Override
            public float z() {
                return z2;
            }

            // @Override
            // public float u() { return u2; }
            //
            // @Override
            // public float v() { return v2; }
            //
            // @Override
            // public int color() { return vcolor2; }
            //
            // @Override
            // public float normalX() { return normalX2; }
            //
            // @Override
            // public float normalY() { return normalY2; }
            //
            // @Override
            // public float normalZ() { return normalZ2; }

        }

        private float x3;
        private float y3;
        private float z3;
        // private float u3;
        // private float v3;
        // private int vcolor3;
        // private float normalX3;
        // private float normalY3;
        // private float normalZ3;

        private class V3 implements IIdealVertex {

            @Override
            public float x() {
                return x3;
            }

            @Override
            public float y() {
                return y3;
            }

            @Override
            public float z() {
                return z3;
            }

            // @Override
            // public float u() { return u3; }
            //
            // @Override
            // public float v() { return v3; }
            //
            // @Override
            // public int color() { return vcolor3; }
            //
            // @Override
            // public float normalX() { return normalX3; }
            //
            // @Override
            // public float normalY() { return normalY3; }
            //
            // @Override
            // public float normalZ() { return normalZ3; }

        }

        private final IIdealVertex[] vertices = { new V0(), new V1(), new V2(), new V3() };

        @Override
        public void addVertex(int i, float x, float y, float z, float u, float v, int color) {
            switch (i) {
            case 0:
                this.x0 = x;
                this.y0 = y;
                this.z0 = z;
                break;
            case 1:
                this.x1 = x;
                this.y1 = y;
                this.z1 = z;
                break;
            case 2:
                this.x2 = x;
                this.y2 = y;
                this.z2 = z;
                break;
            case 3:
                this.x3 = x;
                this.y3 = y;
                this.z3 = z;
                break;
            }
        }

        @Override
        public int vertexCount() {
            return 4;
        }

        @Override
        public IIdealVertex getVertex(int i) {
            return this.vertices[i];
        }

        @Override
        public int getVertexSum(int i) {
            switch (i) {
            case 0:
                return (int) (this.x0 + this.y0 + this.z0);
            case 1:
                return (int) (this.x1 + this.y1 + this.z1);
            case 2:
                return (int) (this.x2 + this.y2 + this.z2);
            case 3:
                return (int) (this.x3 + this.y3 + this.z3);
            }
            return 0;
        }
    }

    private class DirectMaker implements IPolyMaker {
        @Override
        public IIdealPolygon makePoly(Random r) {
            IIdealPolygon result = new DirectRefPoly();
            result.addVertex(0, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(1, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(2, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(3, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            return result;
        }

        @Override
        public String label() {
            return "Direct Reference";
        }
    }

    private class DirectBigInitMaker implements IPolyMaker {
        @Override
        public IIdealPolygon makePoly(Random r) {
            int c = r.nextInt();
            c = r.nextInt();
            c = r.nextInt();
            c = r.nextInt();
            IIdealPolygon result = new DirectBigInitPoly(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(),
                    r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(),
                    r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(),
                    r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), c);
            return result;
        }

        @Override
        public String label() {
            return "Direct Reference Big Constructor Final Immutable";
        }
    }

    @SuppressWarnings("unused")
    private class MakerVertexValueReference implements IPolyMaker {
        @Override
        public IIdealPolygon makePoly(Random r) {
            IIdealPolygon result = new DirectVertValuePoly();
            result.addVertex(0, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(1, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(2, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(3, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            return result;
        }

        @Override
        public String label() {
            return "Vertex Value Reference";
        }
    }

    @SuppressWarnings("unused")
    private class MakerVertexValueRArray implements IPolyMaker {
        @Override
        public IIdealPolygon makePoly(Random r) {
            IIdealPolygon result = new VertValueArrayPoly();
            result.addVertex(0, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(1, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(2, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            result.addVertex(3, r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextInt());
            return result;
        }

        @Override
        public String label() {
            return "Vertex Value Array";
        }
    }

    private static final int POLYS_PER_LIST = 20;
    private static final int SLICES = 1000;
    private static final int LISTS_PER_SLICE = 250;
    private static final int LISTS_PER_RUN = SLICES * LISTS_PER_SLICE;
    private static final int POLY_COUNT = LISTS_PER_RUN * POLYS_PER_LIST;
    private static final int THREAD_POOL_SIZE = 8;
    private static final boolean IS_RANDOM_ORDER = true;

    Object[] lists = new Object[LISTS_PER_RUN];
    ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    IPolyMaker[] runners = { new DirectBigInitMaker(), new DirectMaker(), new BasicMaker(), };

    // new MakerVertexValueReference()
    // new MakerVertexValueRArray()
    int[] accessOrder = new int[LISTS_PER_RUN];

    {
        ArrayList<Integer> ordering = new ArrayList<>(LISTS_PER_RUN);

        for (int i = 0; i < LISTS_PER_RUN; i++) {
            ordering.add(i);
        }
        Collections.shuffle(ordering, new Random(57));
        for (int i = 0; i < LISTS_PER_RUN; i++) {
            accessOrder[i] = ordering.get(i);
        }
    }

    @Test
    public void test() {
        System.out.println("Slice count = " + SLICES);
        System.out.println("Lists per slice = " + LISTS_PER_SLICE);
        System.out.println("Lists per run = " + LISTS_PER_RUN);
        System.out.println("Polys per list = " + POLYS_PER_LIST);
        System.out.println("Total polys per run = " + POLY_COUNT);
        System.out.println("Random access order = " + IS_RANDOM_ORDER);
        System.out.println("");

        for (IPolyMaker runner : runners) {
            System.out.println("======================");
            System.out.println(runner.label());
            System.out.println("======================");
            for (int i = 0; i < 5; i++) {
                System.out.println(String.format("Starting run %d", i));

                runCreate(runner);
                System.out.println(String.format("makePoly in-thread run time = %d ns", runTime.get() / POLY_COUNT));

                runExternalAccess(runner);
                System.out.println(
                        String.format("External access in-thread run time = %d ns", runTime.get() / POLY_COUNT));

                runInternalAccess(runner);
                System.out.println(
                        String.format("Internal access in-thread run time = %d ns", runTime.get() / POLY_COUNT));

                Arrays.fill(lists, 0, lists.length, null);
                System.out.println("");
            }
            System.gc();

            System.out.println("");
        }
    }

    private static final AtomicLong runTime = new AtomicLong();

    private void runCreate(IPolyMaker factory) {
        runTime.set(0);
        ArrayList<Callable<Void>> calls = new ArrayList<>();

        for (int i = 0; i < SLICES; i++) {
            calls.add(queueCreateTask(i * LISTS_PER_SLICE, factory));
        }

        try {
            pool.invokeAll(calls);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Callable<Void> queueCreateTask(int startIndex, IPolyMaker factory) {
        Random r = new Random(startIndex);

        final int endIndex = startIndex + LISTS_PER_SLICE;

        return new Callable<Void>() {
            @Override
            public Void call() {
                for (int i = startIndex; i < endIndex; i++) {
                    ImmutableList.Builder<IIdealPolygon> builder = ImmutableList.builder();
                    for (int j = 0; j < POLYS_PER_LIST; j++) {
                        final long start = System.nanoTime();
                        builder.add(factory.makePoly(r));
                        final long end = System.nanoTime();
                        runTime.addAndGet(end - start);
                    }
                    lists[i] = builder.build();
                }
                return null;
            }
        };
    }

    private AtomicInteger dump = new AtomicInteger();

    private void runExternalAccess(IPolyMaker factory) {
        runTime.set(0);
        ArrayList<Callable<Void>> calls = new ArrayList<>();

        for (int i = 0; i < SLICES; i++) {
            calls.add(queueExternalAccessTask(i * LISTS_PER_SLICE, factory));
        }

        try {
            pool.invokeAll(calls);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Callable<Void> queueExternalAccessTask(int startIndex, IPolyMaker factory) {
        final int endIndex = startIndex + LISTS_PER_SLICE;

        return new Callable<Void>() {
            @SuppressWarnings("unchecked")
            @Override
            public Void call() {
                for (int i = startIndex; i < endIndex; i++) {
                    List<IIdealPolygon> list = IS_RANDOM_ORDER ? (List<IIdealPolygon>) lists[accessOrder[i]]
                            : (List<IIdealPolygon>) lists[i];
                    int trash = 0;
                    for (IIdealPolygon p : list) {
                        final long start = System.nanoTime();
                        trash += factory.accessPolyExternal(p);
                        final long end = System.nanoTime();
                        runTime.addAndGet(end - start);
                    }
                    dump.addAndGet(trash);
                }
                return null;
            }
        };
    }

    private void runInternalAccess(IPolyMaker factory) {
        runTime.set(0);
        ArrayList<Callable<Void>> calls = new ArrayList<>();

        for (int i = 0; i < SLICES; i++) {
            calls.add(queueInternalAccessTask(i * LISTS_PER_SLICE, factory));
        }

        try {
            pool.invokeAll(calls);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Callable<Void> queueInternalAccessTask(int startIndex, IPolyMaker factory) {
        final int endIndex = startIndex + LISTS_PER_SLICE;

        return new Callable<Void>() {
            @SuppressWarnings("unchecked")
            @Override
            public Void call() {
                for (int i = startIndex; i < endIndex; i++) {
                    List<IIdealPolygon> list = IS_RANDOM_ORDER ? (List<IIdealPolygon>) lists[accessOrder[i]]
                            : (List<IIdealPolygon>) lists[i];
                    int trash = 0;
                    for (IIdealPolygon p : list) {
                        final long start = System.nanoTime();
                        trash += factory.accessPolyInternal(p);
                        final long end = System.nanoTime();
                        runTime.addAndGet(end - start);
                    }
                    dump.addAndGet(trash);
                }
                return null;
            }
        };
    }

}