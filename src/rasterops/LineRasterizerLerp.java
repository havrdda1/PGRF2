package rasterops;

import org.jetbrains.annotations.NotNull;
import rasterdata.Image;

public interface LineRasterizerLerp<S,T> {
    /**
     * Rasterizes a line in normalized coordinates ([-1;1] square),
     * upper image left corner in [-1;1], lower left corner in [1;-1]
     * @param image image to "add" the line to
     * @param x1 x-coordinate of the start-point, in [-1;1]
     * @param y1 y-coordinate of the start-point, in [-1;1]
     * @param x2 x-coordinate of the end-point, in [-1;1]
     * @param y2 y-coordinate of the end-point, in [-1;1]
     * @param value1 value of the start-point
     * @param value2 value of the end-point
     * @return new image with the line added on the background
     */
    @NotNull
    Image<T> rasterize(
            @NotNull Image<T> image,
            double x1, double y1, double x2, double y2,
            @NotNull S value1, @NotNull S value2);
}


