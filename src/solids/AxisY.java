package solids;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Point3D;

public class AxisY implements Solid<Point3D, Topology> {

    private final @NotNull
    Seq<Part<Topology>> parts;
    private final @NotNull
    IndexedSeq<Point3D> vertices;
    private final @NotNull IndexedSeq<Integer> indices;

    public AxisY() {
        parts = Array.of(new Part(0, 1, Topology.LINE_LIST));
        vertices = Array.of(new Point3D(0, 0, 0),
                new Point3D(0, 4, 0));
        indices = Stream.rangeClosed(0, 0)
                        .flatMap(i -> Array.of(i, i + 1))
                        .toArray();
    }

    @Override
    public @NotNull Seq<Part<Topology>> getParts() {
        return parts;
    }

    @Override
    public @NotNull IndexedSeq<Point3D> getVertices() {
        return vertices;
    }

    @Override
    public @NotNull IndexedSeq<Integer> getIndices() {
        return indices;
    }

}

