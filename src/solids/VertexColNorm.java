package solids;

import org.jetbrains.annotations.NotNull;
import transforms.Col;
import transforms.Point3D;
import transforms.Vec3D;

public class VertexColNorm {
    private final @NotNull
    Point3D pos;
    private final @NotNull
    Col col;
    private final @NotNull
    Vec3D norm;

    VertexColNorm(@NotNull Point3D pos, @NotNull Col col, @NotNull Vec3D norm) {
        this.pos = pos;
        this.col = col;
        this.norm = norm;
    }

    @NotNull
    public Point3D getPos() {
        return pos;
    }

    @NotNull
    public Col getCol() {
        return col;
    }

    @NotNull
    public Vec3D getNorm() {
        return norm;
    }

    public @NotNull VertexColNorm mul(final double coef) {
        return new VertexColNorm(
                pos.mul(coef), col.mul(coef), norm.mul(coef));
    }

    public @NotNull VertexColNorm add(final @NotNull VertexColNorm rhs) {
        return new VertexColNorm(
                pos.add(rhs.pos), col.add(rhs.col), norm.add(rhs.norm));
    }
}

