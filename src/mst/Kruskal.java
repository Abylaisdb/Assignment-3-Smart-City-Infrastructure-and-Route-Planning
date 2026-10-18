package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;

public final class Kruskal {
    private Kruskal() {}

    public static MSTResult run(Graph g) { return run(g, true, true); }

    public static MSTResult run(Graph g, boolean pathCompression, boolean unionByRank) {
        if (g.isDirected()) throw new IllegalArgumentException("MST needs an undirected graph");
        int n = g.V();
        List<Edge> sorted = new ArrayList<>(g.edges());
        long[] cmp = {0};
        sorted.sort((a, b) -> { cmp[0]++; return Integer.compare(a.weight(), b.weight()); });

        DSU dsu = new DSU(n, pathCompression, unionByRank);
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0;
        for (Edge e : sorted) {
            if (n > 0 && mst.size() == n - 1) break; // tree complete
            examined++;
            if (dsu.union(e.from(), e.to())) {
                mst.add(e);
                total += e.weight();
            }
        }
        boolean connected = n <= 1 || mst.size() == n - 1;
        return new MSTResult("Kruskal", mst, total, connected, cmp[0], examined, dsu.operations(), dsu.pointerSteps());
    }
}
