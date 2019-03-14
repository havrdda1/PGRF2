package rasterops;

import io.vavr.Function3;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import rasterdata.Image;

import java.util.function.Function;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class TriangleRasterizerScan<VertexType, PixelType>
        implements TriangleRasterizer<VertexType, PixelType> {
    private final @NotNull
    Function3<VertexType, VertexType, Double, VertexType> lerp;
    private final @NotNull Function<VertexType, PixelType> pixelShader;

    public TriangleRasterizerScan(@NotNull Function3<VertexType, VertexType, Double, VertexType> lerp, @NotNull Function<VertexType, PixelType> pixelShader) {
        this.lerp = lerp;
        this.pixelShader = pixelShader;
    }

    @NotNull
    @Override
    public Image<PixelType> rasterize(
            final @NotNull Image<PixelType> image,
            final double x1, final double y1,
            final double x2, final double y2,
            final double x3, final double y3,
            final @NotNull VertexType value1,
            final @NotNull VertexType value2,
            final @NotNull VertexType value3) {
        // zaridit iy1 <= iy2 <= iy3
        // neboli zaridit y1 >= y2 >= y3
        if (y1 < y2)
            return rasterize(image, x2, y2, x1, y1, x3, y3, value2, value1, value3);
        if (y2 < y3)
            return rasterize(image, x1, y1, x3, y3, x2, y2, value1, value3, value2);

        final double ix1 = (x1 + 1) * image.getWidth() / 2;
        final double iy1 = (-y1 + 1) * image.getHeight() / 2;
        final double ix2 = (x2 + 1) * image.getWidth() / 2;
        final double iy2 = (-y2 + 1) * image.getHeight() / 2;
        final double ix3 = (x3 + 1) * image.getWidth() / 2;
        final double iy3 = (-y3 + 1) * image.getHeight() / 2;

        final Image<PixelType> firstHalf =
                Stream
                        .rangeClosed(
                                max((int) iy1 + 1, 0),
                                min((int) iy2, image.getHeight() - 1))
                        .foldLeft(image,
                                (currentImage, r) -> {
                                    final double ta = (r - iy1) / (iy2 - iy1);
                                    final double tb = (r - iy1) / (iy3 - iy1);
                                    final double ixa = ix1 * (1 - ta) + ix2 * ta;
                                    final double ixb = ix1 * (1 - tb) + ix3 * tb;
                                    final VertexType va = lerp.apply(value1, value2, ta);
                                    final VertexType vb = lerp.apply(value1, value3, tb);
                                    return doScanLine(currentImage, r, ixa, ixb, va, vb);
                                }
                        );
        final Image<PixelType> result =
                Stream
                        .rangeClosed(
                                max((int) iy2 + 1, 0),
                                min((int) iy3, image.getHeight() - 1))
                        .foldLeft(firstHalf,
                                (currentImage, r) -> {
                                    final double ta = (r - iy3) / (iy2 - iy3);
                                    final double tb = (r - iy1) / (iy3 - iy1);
                                    final double ixa = ix3 * (1 - ta) + ix2 * ta;
                                    final double ixb = ix1 * (1 - tb) + ix3 * tb;
                                    final VertexType va = lerp.apply(value3, value2, ta);
                                    final VertexType vb = lerp.apply(value1, value3, tb);
                                    return doScanLine(currentImage, r, ixa, ixb, va, vb);
                                }
                        );
        return result;
    }
    private @NotNull
    Image<PixelType> doScanLine(
            final @NotNull Image<PixelType> image,
            final int r,
            final double ixa, final double ixb,
            final @NotNull VertexType va,
            final @NotNull VertexType vb) {
        //zaridit ixa <= ixb
        if (ixa > ixb)
            return doScanLine(image, r, ixb, ixa, vb, va);
        return Stream
                .rangeClosed(
                        max((int) ixa + 1, 0),
                        min((int) ixb, image.getWidth() - 1))
                .foldLeft(image,
                        (currentImage, c) -> {
                            final double t = (c - ixa) / (ixb - ixa);
                            final VertexType v = lerp.apply(va, vb, t);
                            return currentImage.withPixel(c, r, pixelShader.apply(v));
                        }
                );
    }
}

