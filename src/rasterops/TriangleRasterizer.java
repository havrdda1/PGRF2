package rasterops;


import org.jetbrains.annotations.NotNull;
import rasterdata.Image;

public interface TriangleRasterizer<S, T> {
    /**
     * Rasterizes a triangle in normalized coordinates ([-1;1] square),
     * upper image left corner in [-1;1], lower left corner in [1;-1]
     * @param image image to "add" the triangle to
     * @param x1 x-coordinate of the first point, in [-1;1]
     * @param y1 y-coordinate of the first point, in [-1;1]
     * @param x2 x-coordinate of the second point, in [-1;1]
     * @param y2 y-coordinate of the second point, in [-1;1]
     * @param x3 x-coordinate of the third point, in [-1;1]
     * @param y3 y-coordinate of the third point, in [-1;1]
     * @param value1 value of the first point
     * @param value2 value of the second point
     * @param value3 value of the third point
     * @return new image with the triangle added on the background
     */
    @NotNull
    Image<T> rasterize(
            @NotNull Image<T> image,
            double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            @NotNull S value1, @NotNull S value2, @NotNull S value3);
}

