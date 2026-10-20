package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class TopologicalSort {
    private TopologicalSort() {}

    public record Result(List<Integer> order, boolean isDag, long operations) {}

    public static Result sort(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("topological order needs a directed graph");
        int n = g.V();
        int[] indeg = new int[n];
        long ops = 0;
        for (Edge e : g.edges()) { indeg[e.to()]++; ops++; }

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) { ops++; if (indeg[v] == 0) queue.add(v); }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            ops++;
            for (Edge e : g.neighbors(u)) {
                ops++;
                if (--indeg[e.to()] == 0) queue.add(e.to());
            }
        }
        boolean dag = order.size() == n;
        return new Result(dag ? order : List.of(), dag, ops);
    }

    public static boolean isValidOrder(Graph g, List<Integer> order) {
        int n = g.V();
        if (order.size() != n) return false;
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || pos[v] != -1) return false;
            pos[v] = i;
        }
        for (Edge e : g.edges()) if (pos[e.from()] >= pos[e.to()]) return false;
        return true;
    }
}
