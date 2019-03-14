package rasterdata;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

public class ImageBlender<T> implements Image<T> {
    private final @NotNull
    Image<T> internalImage;
    private final @NotNull BiFunction<T, T, T> blendFunc;

    public ImageBlender(@NotNull Image<T> internalImage, @NotNull BiFunction<T, T, T> blendFunc) {
        this.internalImage = internalImage;
        this.blendFunc = blendFunc;
    }

    @NotNull
    @Override
    public Optional<T> getPixel(int c, int r) {
        return internalImage.getPixel(c, r);
    }

    @NotNull
    @Override
    public Image<T> withPixel(int c, int r, @NotNull T value) {
        return internalImage.getPixel(c, r).map(
            oldValue ->
                new ImageBlender<>(
                    internalImage.withPixel(c, r, blendFunc.apply(oldValue, value)),
                    blendFunc)
        ).orElse(this);
    }

    @NotNull
    @Override
    public Image<T> cleared(@NotNull T value) {
        return new ImageBlender<>(internalImage.cleared(value), blendFunc);
    }

    @Override
    public int getWidth() {
        return internalImage.getWidth();
    }

    @Override
    public int getHeight() {
        return internalImage.getHeight();
    }
}
