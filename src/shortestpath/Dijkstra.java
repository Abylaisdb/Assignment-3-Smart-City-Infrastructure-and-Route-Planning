package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public final class Dijkstra {
    private Dijkstra() {}

    public static final long INF = Long.MAX_VALUE;

    public static final class Result {
        public final int source;
        public final long[] dist;
        public final int[] prev;
        public long edgeExaminations, relaxations, comparisons, pqOps;

        Result(int source, int n) {
            this.source = source;
            this.dist = new long[n];
            this.prev = new int[n];
            Arrays.fill(dist, INF);
            Arrays.fill(prev, -1);
        }

        public boolean reachable(int v) { return dist[v] != INF; }

        public int reachableCount() {
            int c = 0;
            for (long d : dist) if (d != INF) c++;
            return c;
        }
        public List<Integer> pathTo(int target) {
            if (target < 0 || target >= dist.length) {
                throw new IllegalArgumentException("bad target");
            }
            if (!reachable(target)) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = prev[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph g, int source) {
        int n = g.V();
        if (source < 0 || source >= n) throw new IllegalArgumentException("bad source");
        for (Edge e : g.edges())
            if (e.weight() < 0) throw new IllegalArgumentException("Dijkstra requires non-negative weights: " + e);

        Result res = new Result(source, n);
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> { res.comparisons++; return Long.compare(a[0], b[0]); });
        res.dist[source] = 0;
        pq.add(new long[]{0, source});
        res.pqOps++;
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            res.pqOps++;
            long d = top[0];
            int u = (int) top[1];
            if (d > res.dist[u]) continue; // stale entry
            for (Edge e : g.neighbors(u)) {
                res.edgeExaminations++;
                long nd = d + e.weight();
                if (nd < res.dist[e.to()]) {
                    res.dist[e.to()] = nd;
                    res.prev[e.to()] = u;
                    res.relaxations++;
                    pq.add(new long[]{nd, e.to()});
                    res.pqOps++;
                }
            }
        }
        return res;
    }
}
