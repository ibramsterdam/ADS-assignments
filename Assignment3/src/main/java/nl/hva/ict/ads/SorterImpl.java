package nl.hva.ict.ads;

import java.util.Comparator;
import java.util.List;

public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by selection or insertion sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items List of items
     * @param comparator Interface to compare items
     * @return the items sorted in place
     */
    public List<E> selInsSort(List<E> items, Comparator<E> comparator) {
        for (int i = 1; i < items.size(); ++i) {
            E item = items.get(i);
            int j = i - 1;

            while (j >= 0 && comparator.compare(items.get(j), item) > 0) {
                items.set(j + 1, items.get(j));
                j--;
            }
            items.set(j + 1, item);
        }
        return items;
    }

    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items List of items
     * @param comparator Interface to compare items
     * @return the items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        // sort the complete list of items from position 0 till size-1, encluding position size
        this.quickSortPart(items, 0, items.size() - 1, comparator);
        return items;
    }

    /**
     * Sorts all items between index positions 'from' and 'to' inclusive by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param items List of items
     * @param comparator Interface to compare items
     * @return the items sorted in place
     */
    private void quickSortPart(List<E> items, int from, int to, Comparator<E> comparator) {
        E currItem = items.get((from + to) / 2);
        int newFrom = from;
        int newTo = to;

        while (newFrom <= newTo) {
            while (comparator.compare(items.get(newFrom), currItem) < 0) newFrom++;
            while (comparator.compare(items.get(newTo), currItem) > 0) newTo--;

            if (newFrom <= newTo) {
                swapTwo(items, newFrom, newTo);
                newFrom++;
                newTo--;
            }
        }

        if (from < newTo) {
            quickSortPart(items, from, newTo, comparator);
        }

        if (newFrom < to) {
            quickSortPart(items, newFrom, to, comparator);
        }
    }

    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param numTops    the size of the lead collection of items to be found and sorted
     * @param items List of items
     * @param comparator Interface for comparing
     * @return the items list with its first numTops items sorted according to comparator
     * all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {
        // check 0 < numTops <= items.size()
        if (numTops <= 0) return items;
        else if (numTops > items.size()) return quickSort(items, comparator);

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator.reversed();

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops - 1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection
            swapTwo(items, 0, i);
            heapSink(items, i, reverseComparator);
        }
        // alternatively we can realise full ordening with a partial quicksort:
        // quickSortPart(items, 0, numTops-1, comparator);

        return items;
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0..heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items List of items
     * @param heapSize Place where the heap structure elements will be placed.
     * @param comparator Interface to compare items
     */
    private void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        int childIndex = heapSize - 1;
        int parentIndex = (childIndex - 1) / 2;
        E swimmer = items.get(childIndex);

        while (parentIndex >= 0 && comparator.compare(swimmer, items.get(parentIndex)) < 0) {
            items.set(childIndex, items.get(parentIndex));
            childIndex = parentIndex;
            parentIndex = (childIndex - 1) / 2;
            if (parentIndex == 0) break;
        }

        items.set(childIndex, swimmer);
    }

    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1..heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items List of items
     * @param heapSize Place where the heap structure elements will be placed.
     * @param comparator Interface to compare items
     */
    private void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        int parentIndex = 0;
        int childIndex = 1;
        E sinker = items.get(parentIndex);

        while (childIndex < heapSize) {
            E child = items.get(childIndex);

            if (childIndex + 1 < heapSize && comparator.compare(items.get(childIndex + 1), child) < 0) {
                childIndex++;
                child = items.get(childIndex);
            }

            if (comparator.compare(sinker, child) <= 0) break;
            items.set(parentIndex, child);
            parentIndex = childIndex;
            childIndex = (2 * parentIndex) + 1;
        }

        items.set(parentIndex, sinker);
    }

    private void swapTwo(List<E> items, int firstItemIndex, int secondItemIndex) {
        E oldFirstItem = items.get(firstItemIndex);
        items.set(firstItemIndex, items.get(secondItemIndex));
        items.set(secondItemIndex, oldFirstItem);
    }
}
