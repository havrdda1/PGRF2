package solids;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Point3D;

public class Cube implements Solid<Point3D, Topology> {
    private final @NotNull IndexedSeq<Point3D> vertices;
    private final @NotNull IndexedSeq<Integer> indices;
    private final @NotNull Seq<Part<Topology>> parts;
    public Cube() {
        vertices = Array.of(
                new Point3D(0,0,0),
                new Point3D(1,0,0),
                new Point3D(1,1,0),
                new Point3D(0,1,0),
                new Point3D(0,0,1),
                new Point3D(1,0,1),
                new Point3D(1,1,1),
                new Point3D(0,1,1)
        );
        indices = Stream.rangeClosed(0, 3).flatMap(
                i -> Array.of(i, (i + 1) % 4, i, i + 4, i + 4, (i + 1) % 4 + 4)
        ).appendAll(
                Stream.rangeClosed(0, 3).flatMap(
                        i -> Array.of(
                                i, (i + 1) % 4, (i + 1) % 4 + 4,
                                i, (i + 1) % 4 + 4, i + 4)
                )
        ).appendAll(
                Stream.rangeClosed(0, 1).flatMap(
                        i -> Array.of(
                                i * 4, i * 4 + 1, i * 4 + 3,
                                i * 4 + 1, i * 4 + 2, i * 4 + 3)
                )
        ).toArray();
        parts = Array.of(
                new Part(0, 12, Topology.LINE_LIST),
                new Part(24, 12, Topology.TRIANGLE_LIST));
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

