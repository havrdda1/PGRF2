package rasterops;

import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import rasterdata.Image;

import static java.lang.Math.*;

public class LineRasterizerDDA<PixelType> implements LineRasterizer<PixelType> {
        @NotNull
        @Override
        public Image<PixelType> rasterize(
                @NotNull Image<PixelType> image,
                double x1, double y1, double x2, double y2,
                @NotNull PixelType value) {
            final double ix1 = (x1 + 1) * image.getWidth() / 2;
            final double iy1 = (-y1 + 1) * image.getHeight() / 2;
            final double ix2 = (x2 + 1) * image.getWidth() / 2;
            final double iy2 = (-y2 + 1) * image.getHeight() / 2;
            final double dx = ix2 - ix1, dy = iy2 - iy1,
                    max = max(abs(dx), abs(dy)),
                    ddx = dx / max, ddy = dy / max;
            return Stream.rangeClosed(0, (int) max).foldLeft(image,
                    (currentImage, i) -> currentImage.withPixel(
                            (int) (ix1 + i * ddx),
                            (int) (iy1 + i * ddy), value)
            );
        }
    }
