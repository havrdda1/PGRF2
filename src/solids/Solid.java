package solids;

import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Seq;
import org.jetbrains.annotations.NotNull;

public interface Solid<VertexType, TopoType> {
    class Part<TopologyType> {
        private final int startIndex, numberOfPrimitives;
        private final @NotNull TopologyType topology; // aka conectivity
        public Part(int startIndex, int numberOfPrimitives, @NotNull TopologyType topology) {
            this.startIndex = startIndex;
            this.numberOfPrimitives = numberOfPrimitives;
            this.topology = topology;
        }
        public int getStartIndex() {
            return startIndex;
        }
        public int getNumberOfPrimitives() {
            return numberOfPrimitives;
        }
        public @NotNull TopologyType getTopology() {
            return topology;
        }
    }
    @NotNull IndexedSeq<VertexType> getVertices();
    @NotNull
    IndexedSeq<Integer> getIndices();
    @NotNull
    Seq<Part<TopoType>> getParts();
}

