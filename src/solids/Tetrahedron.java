package solids;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Point3D;

public class Tetrahedron implements Solid<Point3D, Topology> {
    private final @NotNull
    IndexedSeq<Point3D> vertices;
    private final @NotNull IndexedSeq<Integer> indices;
    private final Array parts;

    public Tetrahedron(){
        vertices = Array.of(
                new Point3D(2,2,1),
                new Point3D(1,3,1),
                new Point3D(1,2,1),
                new Point3D(1,2,2));

        indices = Stream.rangeClosed(0, 0).flatMap(
                i -> Array.of(i, i + 1,i + 1, i + 2, i + 2, i, i, i + 3, i + 1, i + 3, i + 2, i + 3)).toArray();
        parts = Array.of(new Part(0, 6, Topology.LINE_LIST));

    }

    @Override
    public @NotNull IndexedSeq<Point3D> getVertices() {
        return vertices;
    }

    @Override
    public @NotNull IndexedSeq<Integer> getIndices() {
        return indices;
    }

    @Override
    public @NotNull
    Seq<Part<Topology>> getParts() {
        return parts;
    }
}

