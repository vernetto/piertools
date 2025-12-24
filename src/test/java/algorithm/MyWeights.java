package algorithm;

import java.util.ArrayList;
import java.util.List;

public class MyWeights {
    static final int[] WEIGHTS = {2, 3, 4};
    static final int MAX_CAPACITY = 8;

    public static void main(String[] args) {
        List<List<Integer>> solutions = new ArrayList<List<Integer>>();
        backtrack(0, new ArrayList<Integer>(), solutions);
    }

    private static void backtrack(int i, ArrayList<Integer> integers, List<List<Integer>> solutions) {
        Integer sum = integers.stream().reduce(0, Integer::sum);
        if (sum <= MAX_CAPACITY) {
            System.out.println("solution found " + integers);
            solutions.add(new ArrayList<>(integers));
        } else {
            System.out.println("over capacity " + integers);
            return;
        }
        for (int index = i; index < WEIGHTS.length; index++) {
            integers.add(WEIGHTS[index]);
            backtrack(index + 1, integers, solutions);
            integers.remove(integers.size() - 1);
        }
    }

}
