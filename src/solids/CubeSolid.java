package solids;

import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import transforms.Col;
import transforms.Point3D;
import transforms.Vec2D;
import transforms.Vec3D;

public class CubeSolid implements Solid<VertexColNorm, Topology> {
    private final @NotNull IndexedSeq<VertexColNorm> vertices;
    private final @NotNull IndexedSeq<Integer> indices;
    private final @NotNull Seq<Part<Topology>> parts;
    public CubeSolid() {
        Seq<Vec2D> positions = Array.of(
                new Vec2D(-1,-1),
                new Vec2D(1,-1),
                new Vec2D(1,1),
                new Vec2D(-1,1)
        );
        Seq<Col> colors = Array.of(
                new Col(1.0,0,0),
                new Col(1.0,1,0),
                new Col(1.0,0,1),
                new Col(0.0,1,0),
                new Col(0.0,0,1),
                new Col(0.0,1,1)
        );
        Seq<Vec3D> normals = Array.of(
                new Vec3D(1,0,0),
                new Vec3D(0,1,0),
                new Vec3D(0,0,1),
                new Vec3D(-1,0,0),
                new Vec3D(0,-1,0),
                new Vec3D(0,0,-1)
        );
        vertices = colors.zip(normals).crossProduct(positions).map(
                colNormPos -> colNormPos.apply(
                        (colNorm, pos) -> colNorm.apply(
                                (col, norm) ->
                                        new VertexColNorm(
                                                new Point3D(
                                                        new Vec3D(1, pos.getX(), pos.getY()).mul(norm.getX()).add(
                                                                new Vec3D(pos.getX(), 1, pos.getY()).mul(norm.getY()).add(
                                                                        new Vec3D(pos.getX(), pos.getY(), 1).mul(norm.getZ())
                                                                )
                                                        ).add(new Vec3D(1,1,1)).mul(0.5)),
                                                col, norm)
                        )
                )
        ).toArray();

        indices = Stream.rangeClosed(0, 5).flatMap(
                face -> Array.of(
                        face * 4, face * 4 + 1, face * 4 + 2,
                        face * 4, face * 4 + 2, face * 4 + 3)
        ).toArray();
        parts = Array.of(new Part<>(0,  12, Topology.TRIANGLE_LIST));
    }
    @NotNull
    @Override
    public IndexedSeq<VertexColNorm> getVertices() {
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

