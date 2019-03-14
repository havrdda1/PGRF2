package rasterdata;

import org.jetbrains.annotations.NotNull;

public interface Presenter<SomePixelType, DeviceType> {
    @NotNull DeviceType present(
            @NotNull Image<SomePixelType> image,
            @NotNull DeviceType device);
}

