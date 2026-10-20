package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public final class SCC {
    private SCC() {}

    public record Result(List<List<Integer>> components, int[] componentId, long operations) {}

    public static Result tarjan(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("SCC needs a directed graph");
        State s = new State(g);
        for (int v = 0; v < g.V(); v++) if (s.index[v] == -1) s.dfs(v);
        return new Result(s.components, s.componentId, s.ops);
    }

    private static final class State {
        final Graph g;
        final int[] index, low, componentId;
        final boolean[] onStack;
        final Deque<Integer> stack = new ArrayDeque<>();
        final List<List<Integer>> components = new ArrayList<>();
        int counter = 0;
        long ops = 0;

        State(Graph g) {
            this.g = g;
            int n = g.V();
            index = new int[n];
            low = new int[n];
            componentId = new int[n];
            onStack = new boolean[n];
            Arrays.fill(index, -1);
            Arrays.fill(componentId, -1);
        }

        void dfs(int v) {
            index[v] = low[v] = counter++;
            stack.push(v);
            onStack[v] = true;
            ops++;
            for (Edge e : g.neighbors(v)) {
                ops++;
                int w = e.to();
                if (index[w] == -1) {
                    dfs(w);
                    low[v] = Math.min(low[v], low[w]);
                } else if (onStack[w]) {
                    low[v] = Math.min(low[v], index[w]);
                }
            }
            if (low[v] == index[v]) { // v is the root of an SCC: pop it
                List<Integer> comp = new ArrayList<>();
                int w;
                do {
                    w = stack.pop();
                    onStack[w] = false;
                    componentId[w] = components.size();
                    comp.add(w);
                } while (w != v);
                components.add(comp);
            }
        }
    }
}
