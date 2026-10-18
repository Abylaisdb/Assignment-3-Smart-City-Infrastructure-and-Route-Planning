package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GraphGenerator {
    private GraphGenerator() {}

    public static Graph connectedUndirected(int n, int m, long seed, int maxW) {
        if (maxW < 1) {
            throw new IllegalArgumentException("maxW must be >= 1");
        }
        long maxEdges = (long) n * (n - 1) / 2;
        if (n < 1 || m < n - 1 || m > maxEdges)
            throw new IllegalArgumentException("need n-1 <= m <= n(n-1)/2");
        Random r = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();

        List<Integer> perm = new ArrayList<>();
        for (int i = 0; i < n; i++) perm.add(i);
        Collections.shuffle(perm, r);
        for (int i = 1; i < n; i++) {
            int u = perm.get(i), v = perm.get(r.nextInt(i));
            used.add(key(u, v, n));
            g.addEdge(u, v, 1 + r.nextInt(maxW));
        }
        while (g.E() < m) {
            int u = r.nextInt(n), v = r.nextInt(n);
            if (u == v) continue;
            if (used.add(key(u, v, n))) g.addEdge(u, v, 1 + r.nextInt(maxW));
        }
        return g;
    }

    public static Graph directed(int n, int m, long seed, int maxW, boolean acyclic) {
        if (maxW < 1) {
            throw new IllegalArgumentException("maxW must be >= 1");
        }
        long maxEdges = acyclic ? (long) n * (n - 1) / 2 : (long) n * (n - 1);
        if (n < 1 || m < 0 || m > maxEdges) throw new IllegalArgumentException("too many edges for n");
        Random r = new Random(seed);
        List<Integer> perm = new ArrayList<>();
        for (int i = 0; i < n; i++) perm.add(i);
        Collections.shuffle(perm, r);
        int[] pos = new int[n];
        for (int i = 0; i < n; i++) pos[perm.get(i)] = i;

        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.E() < m) {
            int u = r.nextInt(n), v = r.nextInt(n);
            if (u == v) continue;
            if (acyclic && pos[u] > pos[v]) { int t = u; u = v; v = t; }
            if (used.add((long) u * n + v)) g.addEdge(u, v, 1 + r.nextInt(maxW));
        }
        return g;
    }

    private static long key(int u, int v, int n) {
        return (long) Math.min(u, v) * n + Math.max(u, v);
    }
}
