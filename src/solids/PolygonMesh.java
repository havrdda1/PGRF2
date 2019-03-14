package solids;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Bicubic;
import transforms.Cubic;
import transforms.Mat4;
import transforms.Point3D;

public class PolygonMesh implements Solid<Point3D, Topology> {
    private final @NotNull IndexedSeq<Point3D> vertices;
    private final @NotNull IndexedSeq<Integer> indices;
    private final Array parts;

    private Bicubic mesh;

    private static final Point3D p11 = new Point3D(5, -2, 1);
    private static final Point3D p12 = new Point3D(4, -2, 1);
    private static final Point3D p13 = new Point3D(3, -2, 1);
    private static final Point3D p14 = new Point3D(2, -2, 1);

    private static final Point3D p21 = new Point3D(5, -1, 1);
    private static final Point3D p22 = new Point3D(4, -1, 2);
    private static final Point3D p23 = new Point3D(3, -1, 2);
    private static final Point3D p24 = new Point3D(2, -1, 1);

    private static final Point3D p31 = new Point3D(5, 0, -1);
    private static final Point3D p32 = new Point3D(4, 0, -1);
    private static final Point3D p33 = new Point3D(3, 0, -1);
    private static final Point3D p34 = new Point3D(2, 0, -1);

    private static final Point3D p41 = new Point3D(5, 1, 1);
    private static final Point3D p42 = new Point3D(4, 1, 2);
    private static final Point3D p43 = new Point3D(3, 1, 2);
    private static final Point3D p44 = new Point3D(2, 1, 1);


    public PolygonMesh(int smoothness) {

        Mat4 curve = Cubic.BEZIER;
        mesh = new Bicubic(curve, p11, p12, p13, p14, p21, p22, p23, p24, p31, p32, p33, p34, p41, p42, p43, p44);

        vertices = Stream.rangeClosed(0, smoothness).flatMap(
                i -> Stream.rangeClosed(0, smoothness).flatMap(
                        j -> Array.of(mesh.compute((double) i / smoothness, (double) j / smoothness))
                )
        ).toArray();

        indices = Stream.rangeClosed(0, smoothness -1).flatMap(
                i -> Stream.rangeClosed(0, smoothness -1).flatMap(
                        j -> Array.of(
                                i * (smoothness + 1) + j,
                                i * (smoothness + 1) + j + 1,
                                (i + 1) * (smoothness + 1) + j,
                                i * (smoothness + 1) + j + 1,
                                (i + 1) * (smoothness + 1) + j,
                                (i + 1) * (smoothness + 1) + j + 1
                        )
                )
        ).toArray();
        parts = Array.of(new Part(0, indices.size() / 3, Topology.TRIANGLE_LIST));
    }
    @NotNull
    @Override
    public IndexedSeq<Point3D> getVertices() {
        return vertices;
    }

    @NotNull
    @Override
    public IndexedSeq<Integer> getIndices() {
        return indices;
    }

    @NotNull
    @Override
    public Seq<Part<Topology>> getParts() {
        return parts;
    }
}

