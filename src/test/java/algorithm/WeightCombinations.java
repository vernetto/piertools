package algorithm;

import java.util.ArrayList;
import java.util.List;

public class WeightCombinations {

    public static void main(String[] args) {
        int[] weights = {2, 3, 4};
        int limit = 8;

        List<List<Integer>> result = new ArrayList<>();
        dfs(weights, 0, new ArrayList<>(), 0, limit, result);

        // Print results
        for (List<Integer> comb : result) {
            System.out.println(comb);
        }
    }

    /**
     * DFS exploring all combinations:
     * - At each index: skip OR take the weight
     * - Prune whenever sum > limit
     */
    private static void dfs(int[] weights,
                            int index,
                            List<Integer> current,
                            int currentSum,
                            int limit,
                            List<List<Integer>> result) {

        // Prune branch: sum too large
        if (currentSum > limit) {
            return;
        }

        // If we reached the end, record the combination
        if (index == weights.length) {
            result.add(new ArrayList<>(current));
            return;
        }

        // OPTION 1: Skip this weight
        dfs(weights, index + 1, current, currentSum, limit, result);

        // OPTION 2: Take this weight
        current.add(weights[index]);
        dfs(weights, index + 1, current, currentSum + weights[index], limit, result);
        current.remove(current.size() - 1); // backtrack
    }
}

