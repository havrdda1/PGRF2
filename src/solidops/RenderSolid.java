package solidops;

import io.vavr.Function3;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import rasterdata.Image;
import rasterops.LineRasterizerLerp;
import rasterops.TriangleRasterizer;
import solids.Topology;
import transforms.Mat4;
import transforms.Point3D;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RenderSolid<PixType, VertType, S> implements
        Renderer<PixType, VertType, Topology> {
    private final @NotNull LineRasterizerLerp<S ,PixType> liner;
    private final @NotNull TriangleRasterizer<S ,PixType> triangler;
    private final @NotNull Function<VertType, Point3D> toPoint3D;
    private final @NotNull BiFunction<VertType, Double, S> toS;
    private final @NotNull Function3<VertType, VertType, Double, VertType> lerp;

    public RenderSolid(@NotNull LineRasterizerLerp<S, PixType> liner, @NotNull TriangleRasterizer<S, PixType> triangler, @NotNull Function<VertType, Point3D> toPoint3D, @NotNull BiFunction<VertType, Double, S> toS, @NotNull Function3<VertType, VertType, Double, VertType> lerp) {
        this.liner = liner;
        this.triangler = triangler;
        this.toPoint3D = toPoint3D;
        this.toS = toS;
        this.lerp = lerp;
    }

    @NotNull
    @Override
    public Image<PixType> render(
            @NotNull Image<PixType> background,
            @NotNull IndexedSeq<VertType> vertices,
            @NotNull IndexedSeq<Integer> indices,
            int startIndex, int numberOfPrimitives,
            @NotNull Topology topology,
            @NotNull Mat4 transform, @NotNull PixType value) {
        switch (topology) {
            case LINE_LIST :
                return Stream.rangeClosed(0, numberOfPrimitives -1)
                             .foldLeft(background,
                                     (currentImage, i) ->
                                             renderEdge(
                                                     currentImage,
                                                     vertices.get(indices.get(
                                                             startIndex + 2 * i
                                                     )),
                                                     vertices.get(indices.get(
                                                             startIndex + 2 * i + 1
                                                     )),
                                                     transform
                                             )
                             );
            case TRIANGLE_LIST:
                return Stream.rangeClosed(0, numberOfPrimitives -1)
                             .foldLeft(background,
                                     (currentImage, i) ->
                                             renderTriangle(
                                                     currentImage,
                                                     vertices.get(indices.get(
                                                             startIndex + 3 * i
                                                     )),
                                                     vertices.get(indices.get(
                                                             startIndex + 3 * i + 1
                                                     )),
                                                     vertices.get(indices.get(
                                                             startIndex + 3 * i + 2
                                                     )),
                                                     transform
                                             )
                             );
        }
        return background;
    }

    private @NotNull
    Image<PixType> renderTriangle(
            final @NotNull Image<PixType> backImage,
            final @NotNull VertType p1,
            final @NotNull VertType p2,
            final @NotNull VertType p3,
            final @NotNull Mat4 transform
    ) {
        final Point3D p1BeforeDehomog = toPoint3D.apply(p1).mul(transform);
        final Point3D p2BeforeDehomog = toPoint3D.apply(p2).mul(transform);
        final Point3D p3BeforeDehomog = toPoint3D.apply(p3).mul(transform);
        return clipTriangle(backImage,
                p1, p2, p3,
                p1BeforeDehomog, p2BeforeDehomog, p3BeforeDehomog);
    }

    private @NotNull
    Image<PixType> clipTriangle(
            final @NotNull Image<PixType> backImage,
            final @NotNull VertType p1,
            final @NotNull VertType p2,
            final @NotNull VertType p3,
            final @NotNull Point3D p1BeforeDehomog,
            final @NotNull Point3D p2BeforeDehomog,
            final @NotNull Point3D p3BeforeDehomog
    ) {
        if (p1BeforeDehomog.getZ() < p2BeforeDehomog.getZ())
            return clipTriangle(backImage,
                    p2, p1, p3,
                    p2BeforeDehomog, p1BeforeDehomog, p3BeforeDehomog);

        if (p2BeforeDehomog.getZ() < p3BeforeDehomog.getZ())
            return clipTriangle(backImage,
                    p1, p3, p2,
                    p1BeforeDehomog, p3BeforeDehomog, p2BeforeDehomog);


        if (p3BeforeDehomog.getZ() >= 0)
            return rasterizeTriangle(backImage,
                    p1, p2, p3,
                    p1BeforeDehomog, p2BeforeDehomog, p3BeforeDehomog);

        if (p2BeforeDehomog.getZ() >= 0) {
            final double ta = p2BeforeDehomog.getZ() /
                    (p2BeforeDehomog.getZ() - p3BeforeDehomog.getZ());
            final VertType pa = lerp.apply(p2, p3, ta);
            final Point3D paBeforeDehomog =
                    p2BeforeDehomog.mul(1 - ta).add(p3BeforeDehomog.mul(ta));

            final double tb = p1BeforeDehomog.getZ() /
                    (p1BeforeDehomog.getZ() - p3BeforeDehomog.getZ());
            final VertType pb = lerp.apply(p1, p3, tb);
            final Point3D pbBeforeDehomog =
                    p1BeforeDehomog.mul(1 - tb).add(p3BeforeDehomog.mul(tb));

            return rasterizeTriangle(
                    rasterizeTriangle(backImage, p1, p2, pa,
                            p1BeforeDehomog, p2BeforeDehomog, paBeforeDehomog),
                    p1, pa, pb,
                    p1BeforeDehomog, paBeforeDehomog, pbBeforeDehomog
            );
        }

        if (p1BeforeDehomog.getZ() >= 0) {
            final double ta = p1BeforeDehomog.getZ() /
                    (p1BeforeDehomog.getZ() - p2BeforeDehomog.getZ());
            final VertType pa = lerp.apply(p1, p2, ta);
            final Point3D paBeforeDehomog =
                    p1BeforeDehomog.mul(1 - ta).add(p2BeforeDehomog.mul(ta));

            final double tb = p1BeforeDehomog.getZ() /
                    (p1BeforeDehomog.getZ() - p3BeforeDehomog.getZ());
            final VertType pb = lerp.apply(p1, p3, tb);
            final Point3D pbBeforeDehomog =
                    p1BeforeDehomog.mul(1 - tb).add(p3BeforeDehomog.mul(tb));

            return rasterizeTriangle(backImage, p1, pa, pb,
                    p1BeforeDehomog, paBeforeDehomog, pbBeforeDehomog
            );
        }

        return backImage;
    }

    private @NotNull
    Image<PixType> rasterizeTriangle(
            final @NotNull Image<PixType> backImage,
            final @NotNull VertType p1,
            final @NotNull VertType p2,
            final @NotNull VertType p3,
            final @NotNull Point3D p1BeforeDehomog,
            final @NotNull Point3D p2BeforeDehomog,
            final @NotNull Point3D p3BeforeDehomog
    ) {
        return p1BeforeDehomog.dehomog().flatMap(
                p1AfterDehomog -> p2BeforeDehomog.dehomog().flatMap(
                        p2AfterDehomog -> p3BeforeDehomog.dehomog().flatMap(
                                p3AfterDehomog ->
                                        Optional.of(triangler.rasterize(
                                                backImage,
                                                p1AfterDehomog.getX(),
                                                p1AfterDehomog.getY(),
                                                p2AfterDehomog.getX(),
                                                p2AfterDehomog.getY(),
                                                p3AfterDehomog.getX(),
                                                p3AfterDehomog.getY(),
                                                toS.apply(p1, p1AfterDehomog.getZ()),
                                                toS.apply(p2, p2AfterDehomog.getZ()),
                                                toS.apply(p3, p3AfterDehomog.getZ())))
                        )
                )
        ).orElse(backImage);
    }

//========================================================
//========================================================
//========================================================

    private @NotNull
    Image<PixType> renderEdge(
            final @NotNull Image<PixType> backImage,
            final @NotNull VertType p1, final @NotNull VertType p2,
            final @NotNull Mat4 transform
    ) {
        final Point3D p1BeforeDehomog = toPoint3D.apply(p1).mul(transform);
        final Point3D p2BeforeDehomog = toPoint3D.apply(p2).mul(transform);
        return clipEdge(backImage, p1, p2, p1BeforeDehomog, p2BeforeDehomog);
    }

    private @NotNull
    Image<PixType> clipEdge(
            final @NotNull Image<PixType> backImage,
            final @NotNull VertType p1, final @NotNull VertType p2,
            final @NotNull Point3D p1BeforeDehomog,
            final @NotNull Point3D p2BeforeDehomog
    ) {
        if (p1BeforeDehomog.getZ() < p2BeforeDehomog.getZ())
            return clipEdge(backImage, p2, p1, p2BeforeDehomog, p1BeforeDehomog);
        if (p2BeforeDehomog.getZ() >= 0)
            return rasterizeEdge(backImage, p1, p2, p1BeforeDehomog, p2BeforeDehomog);
        if (p1BeforeDehomog.getZ() >= 0) {
            final double t = p1BeforeDehomog.getZ() /
                    (p1BeforeDehomog.getZ() - p2BeforeDehomog.getZ());
            final VertType p = lerp.apply(p1, p2, t);
            final Point3D pBeforeDehomog =
                    p1BeforeDehomog.mul(1 - t).add(p2BeforeDehomog.mul(t));
            return rasterizeEdge(backImage, p1, p, p1BeforeDehomog, pBeforeDehomog);
        }
        return backImage;
    }

    private @NotNull
    Image<PixType> rasterizeEdge(
            final @NotNull Image<PixType> backImage,
            final @NotNull VertType p1, final @NotNull VertType p2,
            final @NotNull Point3D p1BeforeDehomog,
            final @NotNull Point3D p2BeforeDehomog
    ) {
        return p1BeforeDehomog.dehomog().flatMap(
                p1AfterDehomog -> p2BeforeDehomog.dehomog().flatMap(
                        p2AfterDehomog ->
                                Optional.of(liner.rasterize(
                                        backImage,
                                        p1AfterDehomog.getX(),
                                        p1AfterDehomog.getY(),
                                        p2AfterDehomog.getX(),
                                        p2AfterDehomog.getY(),
                                        toS.apply(p1, p1AfterDehomog.getZ()),
                                        toS.apply(p2, p2AfterDehomog.getZ())))
                )
        ).orElse(backImage);
    }
}
