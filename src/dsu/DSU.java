package dsu;

public class DSU {
    private final int[] parent;
    private final int[] rank;
    private final boolean pathCompression;
    private final boolean unionByRank;
    private int components;
    private long findCalls, unionCalls, pointerSteps;

    public DSU(int n) { this(n, true, true); }

    public DSU(int n, boolean pathCompression, boolean unionByRank) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0");
        }
        this.parent = new int[n];
        this.rank = new int[n];
        this.pathCompression = pathCompression;
        this.unionByRank = unionByRank;
        this.components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    private void checkIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IllegalArgumentException(
                    "index out of range: " + x
            );
        }
    }

    public int find(int x) {
        findCalls++;
        int root = x;
        while (parent[root] != root) { root = parent[root]; pointerSteps++; }
        if (pathCompression) {
            while (parent[x] != root) { // second pass: re-point the whole path to the root
                int next = parent[x];
                parent[x] = root;
                x = next;
                pointerSteps++;
            }
        }
        return root;
    }
    public boolean union(int a, int b) {
        unionCalls++;
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (unionByRank) {
            if (rank[ra] < rank[rb]) parent[ra] = rb;
            else if (rank[ra] > rank[rb]) parent[rb] = ra;
            else { parent[rb] = ra; rank[ra]++; }
        } else {
            parent[ra] = rb;
        }
        components--;
        return true;
    }

    public boolean connected(int a, int b) { return find(a) == find(b); }
    public int components() { return components; }

    public long operations() { return findCalls + unionCalls; }
    public long pointerSteps() { return pointerSteps; }
}
