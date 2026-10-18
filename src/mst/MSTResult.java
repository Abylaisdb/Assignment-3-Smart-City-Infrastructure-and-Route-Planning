package mst;

import graph.Edge;
import java.util.List;

public record MSTResult(String algorithm, List<Edge> edges, long totalWeight, boolean connected,
                        long comparisons, long edgeExaminations, long structureOps, long extraSteps) {
}
