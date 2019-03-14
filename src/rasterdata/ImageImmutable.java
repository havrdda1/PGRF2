package rasterdata;

import io.vavr.collection.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ImageImmutable<PixelType> implements Image<PixelType> {
    private final @NotNull Vector<PixelType> image;
    private final int width, height;

    private ImageImmutable(final @NotNull Vector<PixelType> image,
                           final int width, final int height) {
        this.image = image;
        this.width = width;
        this.height = height;
    }
    @NotNull
    public static <ValueType> ImageImmutable<ValueType> cleared(
            final int width, final int height,
            final @NotNull ValueType value
    ) {
        return new ImageImmutable<>(
                Vector.fill(width * height, () -> value),
                width, height
        );
    }

    @NotNull
    @Override
    public Optional<PixelType> getPixel(int c, int r) {
        if (0 <= c && c < width && 0 <= r && r < height)
            return Optional.of(image.get(r * width + c));
        return Optional.empty();
    }

    @NotNull
    @Override
    public Image<PixelType> withPixel(int c, int r,
                                      @NotNull PixelType value) {
        if (0 <= c && c < width && 0 <= r && r < height)
            return new ImageImmutable<>(
                    image.update(r * width + c, value),
                    width, height
            );
        return this;
    }

    @NotNull
    @Override
    public Image<PixelType> cleared(@NotNull PixelType value) {
        return cleared(width, height, value);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}

