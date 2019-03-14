package rasterdata;

import org.jetbrains.annotations.NotNull;
import transforms.Col;

public class ZPixel {
    private final @NotNull
    Col col;
    private final double z;

    public ZPixel(@NotNull Col col, double z) {
        this.col = col;
        this.z = z;
    }

    public @NotNull Col getCol() {
        return col;
    }

    public double getZ() {
        return z;
    }

    public @NotNull
    ZPixel mul(final double coef) {
        return new ZPixel(
                col.mul(coef), z * coef);
    }

    public @NotNull
    ZPixel add(final @NotNull ZPixel rhs) {
        return new ZPixel(
                col.add(rhs.col), z + rhs.z);
    }
}
