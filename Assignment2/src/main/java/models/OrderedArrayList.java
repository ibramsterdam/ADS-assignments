package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;

public class OrderedArrayList<E>
        extends ArrayList<E>
        implements OrderedList<E> {

    protected Comparator<? super E> ordening;   // the comparator that has been used with the latest sort
    protected int nSorted;                      // the number of items that have been ordered by barcode in the list
    // representation-invariant
    //      all items at index positions 0 <= index < nSorted have been ordered by the given ordening comparator
    //      other items at index position nSorted <= index < size() can be in any order amongst themselves
    //              and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> ordening) {
        super();
        this.ordening = ordening;
        this.nSorted = 0;
    }

    public Comparator<? super E> getOrdening() {
        return this.ordening;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> comparator) {
        super.sort(comparator);
        this.ordening = comparator;
        this.nSorted = this.size();
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        if (index <= nSorted) nSorted = index;
    }

    @Override
    public E remove(int index) {
        if (index < nSorted) nSorted -= 1;

        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        if (indexOf(o) < nSorted) nSorted -= 1;

        return super.remove(o);
    }

    @Override
    public void sort() {
        if (this.nSorted < this.size()) this.sort(this.ordening);
    }

    @Override
    public int indexOf(Object item) {
        if (item != null) return indexOfByIterativeBinarySearch((E) item);
        return -1;
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) return indexOfByRecursiveBinarySearch(searchItem);
        return -1;
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        int begin = 0, end = nSorted - 1;

        while (begin <= end) {
            int place = (begin + end) / 2; // the middle
            int compareResult = this.ordening.compare(get(place), searchItem);

            if (compareResult == 0) {
                return place;
            } else if (compareResult > 0) {
                end = place - 1;
            } else {
                begin = place + 1;
            }
        }

        return findUnsortedItem(searchItem);
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        return indexOfByRecursiveBinarySearch(searchItem, 0, nSorted - 1);
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @param begin      the first index of the possible range
     * @param end        the last index of the possible range
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem, int begin, int end) {
        int place = (begin + end) / 2;

        if (place < this.size() && begin <= end) {
            int compareResult = this.ordening.compare(get(place), searchItem);

            if (compareResult == 0) {
                return place;
            } else if (compareResult > 0) {
                return indexOfByRecursiveBinarySearch(searchItem, begin, place - 1);
            } else {
                return indexOfByRecursiveBinarySearch(searchItem, place + 1, end);
            }
        }

        return findUnsortedItem(searchItem);
    }

    /**
     * Searches the unsorted part of the list for the item.
     *
     * @param searchItem the item you want to find
     * @return the index of the item you seatched for. -1 if the item is nt found
     */
    private int findUnsortedItem(E searchItem) {
        for (int i = Math.max(nSorted, 0); i < this.size(); i++) {
            if (get(i) == searchItem) return i;
        }

        return -1;
    }

    /**
     * finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem the item you want to add/merge into the list.
     * @param merger  a function that takes two items and returns an item that contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could add the value of attribute X of the second item
     *                to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null) return false;
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem);

        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            E matchedItem = this.get(matchedItemIndex);
            this.set(matchedItemIndex, merger.apply(matchedItem, newItem));
            return false;
        }
    }
}
