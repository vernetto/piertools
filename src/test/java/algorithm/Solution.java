package algorithm;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Solution {
    List<Integer> weights = new ArrayList<>();

    public Solution(Solution currentSetup) {
        weights.addAll(currentSetup.weights);
    }

    public void addWeight(int weight) {
        weights.add(weight);
    }

    public void removeLast() {
        getWeights().removeLast();
    }
}
