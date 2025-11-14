package algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Weights {
    static final int[] WEIGHTS = {2, 3, 4};
    static final int MAX_CAPACITY = 8;

    public static void main(String[] args) {

        List<List<Integer>> results = new ArrayList<>();
        backtrack(0, new ArrayList<>(), results);

        System.out.println("All possible combinations under " + MAX_CAPACITY + "kg:");
        for (List<Integer> combo : results) {
            System.out.println(combo);
        }
    }

    private static void backtrack(int start,
                                  List<Integer> current, List<List<Integer>> results) {
        int sum = current.stream().mapToInt(Integer::intValue).sum();
        if (sum <= MAX_CAPACITY) {
            log.info("solution found {}", current);
            results.add(new ArrayList<>(current));
        } else {
            log.info("over capacity {}", current);
            return; // stop exploring if over capacity
        }

        for (int i = start; i < WEIGHTS.length; i++) {
            current.add(WEIGHTS[i]);
            backtrack(i + 1, current, results);
            current.removeLast();
        }
    }
}
