package rasterdata;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Image<PixelType> {
    /**
     * Returns the value of pixel at the given address
     * @param c column address
     * @param r row address
     * @return {@link Optional} of the value if pixel address is valid,
     *          empty {@link Optional} otherwise
     */
    @NotNull
    Optional<PixelType> getPixel(int c, int r);

    /**
     * Returns a new image with the given pixel set to the given value
     * @param c column address
     * @param r row address
     * @param value new pixel value
     * @return a new image if address is valid, the same image otherwise
     */
    @NotNull
    Image<PixelType> withPixel(int c, int r, @NotNull PixelType value);

    /**
     * Returns a new image filled with given value
     * @param value the value for all pixels
     * @return new image
     */
    @NotNull
    Image<PixelType> cleared(@NotNull PixelType value);

    /**
     * Returns the number of columns
     * @return column count
     */
    int getWidth();
    /**
     * Returns the number of rows
     * @return row count
     */
    int getHeight();
}
