package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {}

    public static MSTResult run(Graph g) {
        if (g.isDirected()) throw new IllegalArgumentException("MST needs an undirected graph");
        int n = g.V();
        boolean[] inTree = new boolean[n];
        List<Edge> mst = new ArrayList<>();
        long[] cmp = {0};
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> { cmp[0]++; return Integer.compare(a[0], b[0]); });
        long total = 0, examined = 0, pqOps = 0;
        int components = 0;

        for (int s = 0; s < n; s++) {
            if (inTree[s]) continue;
            components++;
            inTree[s] = true;
            for (Edge e : g.neighbors(s)) {
                examined++;
                if (!inTree[e.to()]) { pq.add(new int[]{e.weight(), e.from(), e.to()}); pqOps++; }
            }
            while (!pq.isEmpty()) {
                int[] top = pq.poll();
                pqOps++;
                int v = top[2];
                if (inTree[v]) continue; // stale entry
                inTree[v] = true;
                mst.add(new Edge(top[1], v, top[0]));
                total += top[0];
                for (Edge e : g.neighbors(v)) {
                    examined++;
                    if (!inTree[e.to()]) { pq.add(new int[]{e.weight(), e.from(), e.to()}); pqOps++; }
                }
            }
        }
        return new MSTResult("Prim", mst, total, components <= 1, cmp[0], examined, pqOps, 0);
    }
}
