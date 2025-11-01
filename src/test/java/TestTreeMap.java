import es.urjc.grafo.EDA.searchtree.TreeMap;
import es.urjc.grafo.EDA.util.Entry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TestTreeMap {

    private TreeMap<Integer, String> buildMap(Integer... keys) {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (Integer k : keys) {
            map.put(k, "v" + k);
        }
        return map;
    }

    @Test
    public void emptyMapBasics() {
        TreeMap<Integer, String> map = new TreeMap<>();
        Assertions.assertEquals(0, map.size());
        Assertions.assertNull(map.get(1));
        Assertions.assertNull(map.firstEntry());
        Assertions.assertNull(map.lastEntry());
        Assertions.assertNull(map.floorEntry(10));
        Assertions.assertNull(map.ceilingEntry(10));
        Assertions.assertNull(map.previousEntry(10));
        Assertions.assertNull(map.nextEntry(10));
    }

    @Test
    public void putGetAndReplace() {
        TreeMap<Integer, String> map = new TreeMap<>();
        Assertions.assertNull(map.put(5, "five"));
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals("five", map.get(5));

        // replace existing value
        String old = map.put(5, "FIVE");
        Assertions.assertEquals("five", old);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals("FIVE", map.get(5));

        // add more
        map.put(3, "three");
        map.put(7, "seven");
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals("three", map.get(3));
        Assertions.assertEquals("seven", map.get(7));
    }

    @Test
    public void firstAndLastEntry() {
        TreeMap<Integer, String> map = buildMap(5, 3, 7, 2, 4, 6, 8);
        Entry<Integer, String> first = map.firstEntry();
        Entry<Integer, String> last = map.lastEntry();
        Assertions.assertNotNull(first);
        Assertions.assertNotNull(last);
        Assertions.assertEquals(2, first.getKey());
        Assertions.assertEquals("v2", first.getValue());
        Assertions.assertEquals(8, last.getKey());
        Assertions.assertEquals("v8", last.getValue());
    }

    @Test
    public void traversalOrderOfEntrySetIsSortedByKey() {
        TreeMap<Integer, String> map = buildMap(5, 3, 7, 2, 4, 6, 8);
        List<Integer> expected = Arrays.asList(2, 3, 4, 5, 6, 7, 8);
        List<Integer> actual = new ArrayList<>();
        for (Entry<Integer, String> e : map.entrySet()) {
            actual.add(e.getKey());
        }
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void ceilingFloorLowerHigherVariants() {
        TreeMap<Integer, String> map = buildMap(10, 20, 30);

        // exact matches
        Assertions.assertEquals(20, map.floorEntry(20).getKey());
        Assertions.assertEquals(20, map.ceilingEntry(20).getKey());
        Assertions.assertEquals(10, map.previousEntry(20).getKey());
        Assertions.assertEquals(30, map.nextEntry(20).getKey());

        // between keys
        Assertions.assertEquals(20, map.floorEntry(25).getKey());
        Assertions.assertEquals(30, map.ceilingEntry(25).getKey());
        Assertions.assertEquals(20, map.previousEntry(25).getKey());
        Assertions.assertEquals(30, map.nextEntry(25).getKey());

        // extremes
        Assertions.assertNull(map.previousEntry(10));
        Assertions.assertNull(map.floorEntry(5));
        Assertions.assertNull(map.nextEntry(30));
        Assertions.assertNull(map.ceilingEntry(35));
    }

    @Test
    public void removeLeafNode() {
        // Construct a tree where 2 will be a leaf (with external children)
        TreeMap<Integer, String> map = buildMap(5, 3, 7, 2, 4);
        Assertions.assertEquals(5, map.size());
        Assertions.assertEquals("v2", map.get(2));
        String removed = map.remove(2);
        Assertions.assertEquals("v2", removed);
        Assertions.assertNull(map.get(2));
        Assertions.assertEquals(4, map.size());
        // Check order remains valid
        List<Integer> keys = new ArrayList<>();
        for (Entry<Integer, String> e : map.entrySet()) keys.add(e.getKey());
        Assertions.assertEquals(Arrays.asList(3, 4, 5, 7), keys);
    }

    @Test
    public void removeNodeWithOneChildAndRootRemoval() {
        // Build a skew on one side to ensure a node with one child
        TreeMap<Integer, String> map = buildMap(1, 2, 3); // 1 has a right child chain
        Assertions.assertEquals(3, map.size());

        // Remove 2 (has one child: 3)
        String removed2 = map.remove(2);
        Assertions.assertEquals("v2", removed2);
        Assertions.assertEquals(2, map.size());
        Assertions.assertNull(map.get(2));
        // Remaining keys should be 1 and 3
        List<Integer> keys = new ArrayList<>();
        for (Entry<Integer, String> e : map.entrySet()) keys.add(e.getKey());
        Assertions.assertEquals(Arrays.asList(1, 3), keys);

        // Remove root with one child now (depending on structure, root might be 1)
        // Ensure removing existing root does not corrupt map
        Integer rootKey = map.firstEntry().getKey();
        String removedRoot = map.remove(rootKey);
        Assertions.assertNotNull(removedRoot);
        Assertions.assertEquals(1, map.size());
        // Only one key remains
        Iterator<Entry<Integer, String>> it = map.entrySet().iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(3, it.next().getKey());
        Assertions.assertFalse(it.hasNext());
    }

    @Test
    public void removeNodeWithTwoChildren() {
        TreeMap<Integer, String> map = buildMap(5, 3, 7, 2, 4, 6, 8);
        Assertions.assertEquals(7, map.size());
        // Remove root which has two children
        String removed = map.remove(5);
        Assertions.assertEquals("v5", removed);
        Assertions.assertEquals(6, map.size());
        Assertions.assertNull(map.get(5));
        // In-order keys should still be sorted and complete
        List<Integer> keys = new ArrayList<>();
        for (Entry<Integer, String> e : map.entrySet()) keys.add(e.getKey());
        Assertions.assertEquals(Arrays.asList(2, 3, 4, 6, 7, 8), keys);
    }

    @Test
    public void removeNonExistingDoesNothing() {
        TreeMap<Integer, String> map = buildMap(1, 2, 3);
        Assertions.assertNull(map.remove(4));
        Assertions.assertEquals(3, map.size());
    }

    @Test
    public void subMapRangeIsInclusiveExclusiveAndOrdered() {
        TreeMap<Integer, String> map = buildMap(1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Integer> rangeKeys = new ArrayList<>();
        for (Entry<Integer, String> e : map.subMap(3, 7)) {
            rangeKeys.add(e.getKey());
        }
        // [3,7) => 3,4,5,6
        Assertions.assertEquals(Arrays.asList(3, 4, 5, 6), rangeKeys);

        // Empty when from >= to
        List<Integer> empty = new ArrayList<>();
        for (Entry<Integer, String> e : map.subMap(7, 3)) {
            empty.add(e.getKey());
        }
        Assertions.assertTrue(empty.isEmpty());

        // toKey is exclusive
        List<Integer> upToFive = new ArrayList<>();
        for (Entry<Integer, String> e : map.subMap(1, 5)) {
            upToFive.add(e.getKey());
        }
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4), upToFive);
    }

    @Test
    public void subMapSnapshotIndependentAndOrdered() {
        TreeMap<Integer, String> map = buildMap(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Iterable<Entry<Integer, String>> snapshot = map.subMapSnapshot(3, 7);

        // First traversal captures the snapshot contents
        List<Integer> keys1 = new ArrayList<>();
        List<String> vals1 = new ArrayList<>();
        for (Entry<Integer, String> e : snapshot) {
            keys1.add(e.getKey());
            vals1.add(e.getValue());
        }
        Assertions.assertEquals(Arrays.asList(3, 4, 5, 6), keys1);

        // Mutate the original map after taking the snapshot
        map.remove(4);                 // remove an element that was in the snapshot
        map.put(5, "CHANGED");       // change value of an element that was in the snapshot
        map.put(10, "v10");          // add element outside the range
        map.put(4, "four");          // reinsert a previously removed key (new Entry instance)

        // Iterate snapshot again: it must remain unchanged (both keys and values)
        List<Integer> keys2 = new ArrayList<>();
        List<String> vals2 = new ArrayList<>();
        for (Entry<Integer, String> e : snapshot) {
            keys2.add(e.getKey());
            vals2.add(e.getValue());
        }
        Assertions.assertEquals(Arrays.asList(3, 4, 5, 6), keys2);
        Assertions.assertEquals(vals1, vals2);
    }

    @Test
    public void subMapSnapshotEmptyWhenFromNotLessThanTo() {
        TreeMap<Integer, String> map = buildMap(1, 2, 3, 4, 5);
        List<Integer> keys = new ArrayList<>();
        for (Entry<Integer, String> e : map.subMapSnapshot(5, 5)) keys.add(e.getKey());
        for (Entry<Integer, String> e : map.subMapSnapshot(7, 3)) keys.add(e.getKey());
        Assertions.assertTrue(keys.isEmpty());
    }

    @Test
    public void entrySetSnapshotIsSortedAndIndependentFromMutations() {
        TreeMap<Integer, String> map = buildMap(5, 3, 7, 2, 4, 6, 8);
        Iterable<Entry<Integer, String>> snapshot = map.entrySetSnapshot();

        List<Integer> keys1 = new ArrayList<>();
        List<String> vals1 = new ArrayList<>();
        for (Entry<Integer, String> e : snapshot) {
            keys1.add(e.getKey());
            vals1.add(e.getValue());
        }
        Assertions.assertEquals(Arrays.asList(2, 3, 4, 5, 6, 7, 8), keys1);
        Assertions.assertEquals(Arrays.asList("v2", "v3", "v4", "v5", "v6", "v7", "v8"), vals1);

        // Mutate the original map: update, remove, and insert
        map.put(5, "FIVE"); // change value
        map.remove(2);       // remove min key
        map.put(1, "v1");   // add new min key

        // The snapshot should remain unchanged
        List<Integer> keys2 = new ArrayList<>();
        List<String> vals2 = new ArrayList<>();
        for (Entry<Integer, String> e : snapshot) {
            keys2.add(e.getKey());
            vals2.add(e.getValue());
        }
        Assertions.assertEquals(Arrays.asList(2, 3, 4, 5, 6, 7, 8), keys2);
        Assertions.assertEquals(Arrays.asList("v2", "v3", "v4", "v5", "v6", "v7", "v8"), vals2);
    }

    @Test
    public void entrySetSnapshotOnEmptyMapIsEmpty() {
        TreeMap<Integer, String> map = new TreeMap<>();
        Iterable<Entry<Integer, String>> snapshot = map.entrySetSnapshot();
        Assertions.assertFalse(snapshot.iterator().hasNext());
    }

}
