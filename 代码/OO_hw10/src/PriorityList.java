import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

public class PriorityList {
    public static class Pair {
        private final Integer first;
        private final Integer second;

        public Pair(Integer first, Integer second) {
            this.first = first;
            this.second = second;
        }

        public Integer getFirst() {
            return first;
        }

        public Integer getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Pair pair = (Pair) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }

    private final Comparator<Pair> comparator = (p1, p2) -> {
        int secondCompare = p2.getSecond().compareTo(p1.getSecond());
        if (secondCompare != 0) {
            return secondCompare;
        }
        return p1.getFirst().compareTo(p2.getFirst());
    };

    private final TreeSet<Pair> set = new TreeSet<>(comparator);
    private final HashMap<Integer, Pair> firstToPairMap = new HashMap<>();

    public void addPair(int first, int second) {
        if (firstToPairMap.containsKey(first)) {
            set.remove(firstToPairMap.get(first));
        }
        Pair pair = new Pair(first, second);
        set.add(pair);
        firstToPairMap.put(first, pair);
    }

    public void removePairByFirst(int first) {
        if (firstToPairMap.containsKey(first)) {
            Pair pair = firstToPairMap.remove(first);
            set.remove(pair);
        }
    }

    public Pair getFirstPair() {
        return set.isEmpty() ? null : set.first();
    }

    public Integer getSecondByFirst(int first) {
        if (firstToPairMap.containsKey(first)) {
            return firstToPairMap.get(first).second;
        }
        return null;
    }

    public void modifySecondByFirst(int first, int value) {
        int second = getSecondByFirst(first) + value;
        removePairByFirst(first);
        addPair(first, second);

    }
}
