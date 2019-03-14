package solidops;

import io.vavr.collection.IndexedSeq;
import org.jetbrains.annotations.NotNull;
import rasterdata.Image;
import solids.Solid;
import transforms.Mat4;

public interface Renderer<PixelType, VertexType, TopologyType> {
    default @NotNull
    Image<PixelType> render(
            @NotNull Image<PixelType> background,
            @NotNull Solid<VertexType, TopologyType> solid,
            @NotNull Mat4 transform,
            @NotNull PixelType value
    ) {
        return solid.getParts().foldLeft(background,
                (currentImage, part) -> render(
                        currentImage,
                        solid.getVertices(),
                        solid.getIndices(),
                        part.getStartIndex(),
                        part.getNumberOfPrimitives(),
                        part.getTopology(),
                        transform,
                        value
                )
        );
    }
    @NotNull
    Image<PixelType> render(
            @NotNull Image<PixelType> background,
            @NotNull IndexedSeq<VertexType> vertices,
            @NotNull IndexedSeq<Integer> indices,
            int startIndex,
            int numberOfPrimitives,
            @NotNull TopologyType topology,
            @NotNull Mat4 transform,
            @NotNull PixelType value
    );
}

