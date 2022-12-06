package nl.hva.ict.ads;
public class Archer {
    public static int MAX_ARROWS = 3;
    public static int MAX_ROUNDS = 10;


    private final int id;
    private String firstName;
    private String lastName;
    private static int lastId = 135788;
    private int[][] scores;

    /**
     * Constructs a new instance of Archer and assigns a unique id to the instance.
     * Each new instance should be assigned a number that is 1 higher than the last one assigned.
     * The first instance created should have ID 135788;
     *
     * @param firstName the archers first name.
     * @param lastName the archers surname.
     */
    public Archer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        id = lastId++;
        this.scores = new int[MAX_ROUNDS][MAX_ARROWS];
    }

    /**
     * Registers the points for each of the three arrows that have been shot during a round.
     *
     * @param round the round for which to register the points. First round has number 1.
     * @param points the points shot during the round, one for each arrow.
     */
    public void registerScoreForRound(int round, int[] points) {
        System.arraycopy(points, 0, scores[round - 1], 0, points.length);
    }


    /**
     * Calculates/retrieves the total score of all arrows across all rounds
     * @return the total score of all arrows across all rounds
     */
    public int getTotalScore() {
        int totalScore = 0;

        for (int i = 0; i < MAX_ROUNDS; i++) {
            for (int j = 0; j < MAX_ARROWS; j++) {
                totalScore += scores[i][j];
            }
        }

        return totalScore;
    }

    /**
     * Calculates/retrieves the total number of misses across all rounds
     * @return the total number of misses across all rounds
     */
    public int getTotalMisses() {
        int totalMisses = 0;

        for (int i = 0; i < MAX_ROUNDS; i++) {
            for (int j = 0; j < MAX_ARROWS; j++) {
                if (scores[i][j] == 0){
                    totalMisses++;
                }
            }
        }

        return totalMisses;
    }

    /**
     * compares the scores/id of this archer with the scores/id of the other archer according to
     * the scoring scheme: highest total points -> least misses -> earliest registration
     * The archer with the lowest id has registered first
     * @param other     the other archer to compare against
     * @return  negative number, zero or positive number according to Comparator convention
     */
    public int compareByHighestTotalScoreWithLeastMissesAndLowestId(Archer other) {
        if (this.getTotalScore() != other.getTotalScore()){
            return Integer.compare(other.getTotalScore(), this.getTotalScore());
        }

        if (this.getTotalMisses() != other.getTotalMisses()){
            return Integer.compare(this.getTotalMisses(), other.getTotalMisses());
        }

        return Integer.compare(this.getId(), other.getId());
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return id + " (" + getTotalScore() + ") " + firstName + ' ' + lastName;
    }
}
