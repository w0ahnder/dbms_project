package optimal;

import java.util.*;

public class CostCalculator {
  public TableSizeCalculator calc;

  public CostCalculator(TableSizeCalculator calc) {
    this.calc = calc;
  }

  public List<String> findOptimalJoinOrder(List<String> relations) {
    int n = relations.size();
    Map<Set<String>, Integer> dp = new HashMap<>();
    Map<Set<String>, Pair<Set<String>, Set<String>>> backtrack = new HashMap<>();

    // Initialize DP for individual relations (base case)
    for (String rel : relations) {
      dp.put(new HashSet<>(Collections.singletonList(rel)), 0);
    }

    // Iterate over subset sizes (from 2 to n)
    for (int size = 2; size <= n; size++) {
      List<Set<String>> subsets = generateSubsets(relations, size);

      for (Set<String> subset : subsets) {
        int minCost = Integer.MAX_VALUE;
        Pair<Set<String>, Set<String>> bestSplit = null;

        // Partition the subset into two smaller subsets
        for (String rel : subset) {
          Set<String> left = new HashSet<>(subset);
          left.remove(rel);
          Set<String> right = new HashSet<>(Collections.singleton(rel));
          int joinCost = 0;
          if (left.size() == 1) {
            joinCost = 0;
          } else {
            joinCost = calc.getTableSize(new ArrayList<>(right));
            calc.getTableSize(new ArrayList<>(right), rel);
          }

          int cost = dp.get(left) + joinCost;
          System.out.println("cost is " + cost);
          if (cost < minCost) {
            minCost = cost;
            bestSplit = new Pair<>(left, right);
          }
        }

        dp.put(subset, minCost);
        backtrack.put(subset, bestSplit);
      }
    }

    // Start backtracking to reconstruct the optimal join order
    Set<String> fullSet = new HashSet<>(relations);
    List<String> optimalOrder = new ArrayList<>();
    reconstructJoinOrder(fullSet, backtrack, optimalOrder);

    return optimalOrder;
  }

  private void reconstructJoinOrder(
      Set<String> subset,
      Map<Set<String>, Pair<Set<String>, Set<String>>> backtrack,
      List<String> optimalOrder) {
    if (subset.size() == 1) {
      optimalOrder.addAll(subset);
      return;
    }

    Pair<Set<String>, Set<String>> split = backtrack.get(subset);
    reconstructJoinOrder(split.getLeft(), backtrack, optimalOrder);
    reconstructJoinOrder(split.getRight(), backtrack, optimalOrder);
  }

  private List<Set<String>> generateSubsets(List<String> relations, int size) {
    List<Set<String>> subsets = new ArrayList<>();
    generateSubsetsHelper(relations, 0, size, new HashSet<>(), subsets);
    return subsets;
  }

  private void generateSubsetsHelper(
      List<String> relations, int start, int size, Set<String> current, List<Set<String>> subsets) {
    if (current.size() == size) {
      subsets.add(new HashSet<>(current));
      return;
    }
    for (int i = start; i < relations.size(); i++) {
      current.add(relations.get(i));
      generateSubsetsHelper(relations, i + 1, size, current, subsets);
      current.remove(relations.get(i));
    }
  }

  // Helper class for pair representation
  private static class Pair<L, R> {
    private L left;
    private R right;

    public Pair(L left, R right) {
      this.left = left;
      this.right = right;
    }

    public L getLeft() {
      return left;
    }

    public R getRight() {
      return right;
    }
  }
}
