package com.pierre.madagascarsolitaire;

import java.util.*;

/**
 * Peg Solitaire (English 7x7 cross board = 33 valid holes).
 * - Bitboard state: 1 bit = peg present
 * - Precomputed legal moves (from, over, to)
 * - Heuristic move ordering (center-weight delta)
 * - Dead-end cache to prune states already proven unsolvable
 *
 * Prints ONE solution (sequence of moves).
 */
public class PegSolitaireWeightedSolver {

    // Board is 7x7; only 33 cells are valid holes.
    private static final int N = 7;
    private static final int INVALID = -1;

    // Map (r,c) -> holeIndex [0..32], or -1 if invalid.
    private static final int[][] RC_TO_IDX = new int[N][N];
    // Map holeIndex -> (r,c)
    private static final int[] IDX_TO_R = new int[33];
    private static final int[] IDX_TO_C = new int[33];

    // Precomputed moves (from, over, to), all indices in [0..32]
    private static final List<Move> ALL_MOVES = new ArrayList<>();

    // Heuristic weights per hole index (higher = “better to have a peg here”)
    // Strongly favors center-ish positions.
    private static final int[] W = new int[33];

    // Start state: all pegs except center empty
    private static long START;

    // Option: require final peg at center (classic goal)
    private static final boolean REQUIRE_CENTER_FINISH = true;
    private static int CENTER_IDX;

    // Dead-end cache: states for which no solution exists (from that state)
    private static final HashSet<Long> DEAD = new HashSet<>(1 << 20);

    private static final ArrayList<Move> PATH = new ArrayList<>();
    private static boolean solved = false;

    public static void main(String[] args) {
        initBoardMapping();
        initMoves();
        initWeights();
        initStartState();

        // Solve
        dfs(START);

        if (!solved) {
            System.out.println("No solution found.");
        }
    }

    // -------------------- Core search --------------------

    private static boolean dfs(long state) {
        if (solved) return true;

        int pegs = Long.bitCount(state);
        if (pegs == 1) {
            if (!REQUIRE_CENTER_FINISH || ((state & (1L << CENTER_IDX)) != 0)) {
                printSolution();
                solved = true;
                return true;
            }
            return false;
        }

        if (DEAD.contains(state)) return false;

        // Gather applicable moves
        ArrayList<ScoredMove> moves = new ArrayList<>();
        for (Move m : ALL_MOVES) {
            if (isApplicable(state, m)) {
                int score = heuristicDelta(state, m);
                moves.add(new ScoredMove(m, score));
            }
        }

        if (moves.isEmpty()) {
            DEAD.add(state);
            return false;
        }

        // Sort by descending score (best-first), tie-break by a small secondary key
        moves.sort((a, b) -> {
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            // Slight tie-break: prefer moves that land closer to center (by weight)
            return Integer.compare(W[b.m.to], W[a.m.to]);
        });

        // Try moves in that order
        for (ScoredMove sm : moves) {
            Move m = sm.m;
            long next = apply(state, m);
            PATH.add(m);

            if (dfs(next)) return true;

            PATH.remove(PATH.size() - 1);
        }

        DEAD.add(state);
        return false;
    }

    /**
     * Heuristic: prefer moves that increase "center-ness" and remove outer pegs.
     * Delta of weighted peg-sum after move:
     * - from peg removed => -W[from]
     * - over peg removed => -W[over]
     * + to peg added     => +W[to]
     *
     * Larger delta is better.
     */
    private static int heuristicDelta(long state, Move m) {
        int delta = W[m.to] - W[m.from] - W[m.over];

        // Bonus: prefer moves that reduce “fragmentation” a bit (cheap local check)
        // Encourage landing where there will be more neighboring pegs (mobility-ish).
        delta += 2 * countAdjacentPegs(state, m.to);

        // Bonus: avoid creating isolated single pegs on outer ring (very rough)
        // i.e., if "from" was outer-ish, removing it can be good; if "to" is outer-ish, bad.
        delta += (W[m.to] - W[m.from]) / 4;

        return delta;
    }

    private static int countAdjacentPegs(long state, int idx) {
        int r = IDX_TO_R[idx], c = IDX_TO_C[idx];
        int count = 0;
        count += hasPegAtRC(state, r - 1, c) ? 1 : 0;
        count += hasPegAtRC(state, r + 1, c) ? 1 : 0;
        count += hasPegAtRC(state, r, c - 1) ? 1 : 0;
        count += hasPegAtRC(state, r, c + 1) ? 1 : 0;
        return count;
    }

    private static boolean hasPegAtRC(long state, int r, int c) {
        if (r < 0 || r >= N || c < 0 || c >= N) return false;
        int idx = RC_TO_IDX[r][c];
        if (idx == INVALID) return false;
        return (state & (1L << idx)) != 0;
    }

    // -------------------- Bitboard operations --------------------

    private static boolean isApplicable(long state, Move m) {
        long fromBit = 1L << m.from;
        long overBit = 1L << m.over;
        long toBit   = 1L << m.to;

        // from & over must have pegs, to must be empty
        return (state & fromBit) != 0
                && (state & overBit) != 0
                && (state & toBit) == 0;
    }

    private static long apply(long state, Move m) {
        // remove from + over, add to
        state &= ~(1L << m.from);
        state &= ~(1L << m.over);
        state |=  (1L << m.to);
        return state;
    }

    // -------------------- Initialization --------------------

    private static void initBoardMapping() {
        for (int r = 0; r < N; r++) Arrays.fill(RC_TO_IDX[r], INVALID);

        int idx = 0;
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                boolean valid = !((r < 2 || r > 4) && (c < 2 || c > 4)); // classic cross
                if (valid) {
                    RC_TO_IDX[r][c] = idx;
                    IDX_TO_R[idx] = r;
                    IDX_TO_C[idx] = c;
                    idx++;
                }
            }
        }
        if (idx != 33) throw new IllegalStateException("Expected 33 valid holes, got " + idx);

        CENTER_IDX = RC_TO_IDX[3][3];
    }

    private static void initMoves() {
        // For each hole as "from", see if 2 steps in each direction yields valid "to" with valid "over"
        int[][] DIRS = { {-1,0}, {1,0}, {0,-1}, {0,1} };

        for (int from = 0; from < 33; from++) {
            int r = IDX_TO_R[from];
            int c = IDX_TO_C[from];

            for (int[] d : DIRS) {
                int or = r + d[0];
                int oc = c + d[1];
                int tr = r + 2*d[0];
                int tc = c + 2*d[1];

                if (inside(or, oc) && inside(tr, tc)) {
                    int over = RC_TO_IDX[or][oc];
                    int to   = RC_TO_IDX[tr][tc];
                    if (over != INVALID && to != INVALID) {
                        ALL_MOVES.add(new Move(from, over, to));
                    }
                }
            }
        }
        // Note: this includes both directions naturally due to scanning all "from".
    }

    private static void initWeights() {
        // Weight based on Manhattan distance to center (3,3), then shaped a bit.
        // Higher is better (keep pegs near center).
        for (int i = 0; i < 33; i++) {
            int r = IDX_TO_R[i], c = IDX_TO_C[i];
            int dist = Math.abs(r - 3) + Math.abs(c - 3);
            // base: center=10, then decreases with distance
            int base = 10 - 2 * dist;
            // clamp lower bound to avoid too negative
            W[i] = Math.max(-8, base);
        }

        // Slightly boost the very center and near-center cross
        W[CENTER_IDX] += 6;

        // Boost the four orthogonal neighbors of center
        int up = RC_TO_IDX[2][3], dn = RC_TO_IDX[4][3], lf = RC_TO_IDX[3][2], rt = RC_TO_IDX[3][4];
        W[up] += 2; W[dn] += 2; W[lf] += 2; W[rt] += 2;
    }

    private static void initStartState() {
        // All valid holes filled, except center empty
        long s = 0;
        for (int i = 0; i < 33; i++) s |= (1L << i);
        s &= ~(1L << CENTER_IDX);
        START = s;
    }

    private static boolean inside(int r, int c) {
        return r >= 0 && r < N && c >= 0 && c < N;
    }

    // -------------------- Output --------------------

    private static void printSolution() {
        System.out.println("Solution found! Moves: " + PATH.size());
        for (int i = 0; i < PATH.size(); i++) {
            Move m = PATH.get(i);
            System.out.printf(
                    "%2d) (%d,%d) -> (%d,%d) over (%d,%d)%n",
                    i + 1,
                    IDX_TO_R[m.from], IDX_TO_C[m.from],
                    IDX_TO_R[m.to],   IDX_TO_C[m.to],
                    IDX_TO_R[m.over], IDX_TO_C[m.over]
            );
        }
    }

    // -------------------- Small structs --------------------

    private static final class Move {
        final int from, over, to;
        Move(int from, int over, int to) {
            this.from = from;
            this.over = over;
            this.to = to;
        }
    }

    private static final class ScoredMove {
        final Move m;
        final int score;
        ScoredMove(Move m, int score) {
            this.m = m;
            this.score = score;
        }
    }
}
