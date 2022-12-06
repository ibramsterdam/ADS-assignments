package nl.hva.ict.ads;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.*;

public class executionTimeTest {
    protected Sorter<Archer> sorter = new ArcherSorter();
    protected static List<List<Archer>> allArcherLists = new ArrayList<>();
    protected Comparator<Archer> scoringScheme = Archer::compareByHighestTotalScoreWithLeastMissesAndLowestId;
    private static final List<Integer> listSizes = new ArrayList<>(Arrays.asList(100, 200, 400, 800, 1600, 3200));
    private static final long NR_OF_TESTS = 10L;
    private static final double MSEC_IN_NSEC = 1E-6;

    //   , 12800 25600,51200, 102400, 204.800

    @BeforeAll
    static void setup() {
        for(int size : listSizes){
            for (long i = 1L; i <= NR_OF_TESTS; i++) {
                ChampionSelector championSelector = new ChampionSelector(i);
                ArrayList<Archer> archers = new ArrayList<>(championSelector.enrollArchers(size));
                Collections.shuffle(archers);
                allArcherLists.add(archers);
            }
        }
    }

    @Test
    void measureExecutionTimesNSort() {
        List<Long> measuredTimes = new ArrayList<>();

        for (List<Archer> archerList: allArcherLists){
            System.gc();
            long start = System.nanoTime();
            sorter.selInsSort(archerList, scoringScheme);
            long stop = System.nanoTime();
            measuredTimes.add(stop - start);
        }

        int numberOfSizes = listSizes.size();

        for (int i = 0; i < numberOfSizes ; i++) {
            long average = 0L;

            for (int j = 0; j < NR_OF_TESTS; j++) {
                average += measuredTimes.get(j + ( i * j));
            }

            System.out.printf("nSort: %d archers nSorted in an average of %f ms\n" ,listSizes.get(i), (average * MSEC_IN_NSEC) );
        }
    }

    @Test
    void measureExecutionTimesQuickSort() {
        List<Long> measuredTimes = new ArrayList<>();

        for (List<Archer> archerList: allArcherLists){
            System.gc();
            long start = System.nanoTime();
            sorter.quickSort(archerList, scoringScheme);
            long stop = System.nanoTime();
            measuredTimes.add(stop - start);
        }

        int numberOfSizes = listSizes.size();

        for (int i = 0; i < numberOfSizes ; i++) {
            long average = 0L;

            for (int j = 0; j < NR_OF_TESTS; j++) {
                average += measuredTimes.get(j + ( i * j));
            }

            System.out.printf("quickSort: %d archers quicksorted in an average of %f ms\n" ,listSizes.get(i), (average * MSEC_IN_NSEC) );
        }
    }
    @Test
    void measureExecutionTimesHeapSort() {
        List<Long> measuredTimes = new ArrayList<>();

        for (List<Archer> archerList: allArcherLists){
            System.gc();
            long start = System.nanoTime();
            sorter.topsHeapSort(archerList.size(), archerList, scoringScheme);
            long stop = System.nanoTime();
            measuredTimes.add(stop - start);
        }

        int numberOfSizes = listSizes.size();

        for (int i = 0; i < numberOfSizes ; i++) {
            long average = 0L;

            for (int j = 0; j < NR_OF_TESTS; j++) {
                average += measuredTimes.get(j + ( i * j));
            }

            System.out.printf("heapSort: %d heapsorted nSorted in an average of %f ms\n" ,listSizes.get(i), (average * MSEC_IN_NSEC) );
        }
    }
}
