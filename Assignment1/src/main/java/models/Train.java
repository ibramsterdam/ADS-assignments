package models;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    public boolean hasWagons() {
        return firstWagon != null;
    }

    public boolean isPassengerTrain() {
        return firstWagon instanceof PassengerWagon;
    }

    public boolean isFreightTrain() {
        return firstWagon instanceof FreightWagon;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     * (sustaining all representation invariants)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     */
    public void setFirstWagon(Wagon wagon) {
        this.firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        return firstWagon == null ? 0 : firstWagon.getTailLength() + 1;
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        return firstWagon == null ? null : firstWagon.getLastWagonAttached();
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        int nrOfSeats = 0;

        if (isPassengerTrain()) {
            PassengerWagon currentWagon = (PassengerWagon) firstWagon;

            for (int i = 0; i <= firstWagon.getTailLength(); i++) {
                nrOfSeats += currentWagon.getNumberOfSeats();
                currentWagon = (PassengerWagon) currentWagon.getNextWagon();
            }
        }

        return nrOfSeats;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        int maxWeight = 0;

        if (isFreightTrain()) {
            FreightWagon currentWagon = (FreightWagon) firstWagon;

            for (int i = 0; i <= firstWagon.getTailLength(); i++) {
                maxWeight += currentWagon.getMaxWeight();
                currentWagon = (FreightWagon) currentWagon.getNextWagon();
            }
        }

        return maxWeight;
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position the position you want to find the wagon in
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        Wagon wagonAtPos = firstWagon;

        if (!(position <= 0 || position > getNumberOfWagons() || wagonAtPos == null)) {
            for (int i = 1; i < position; i++) {
                wagonAtPos = wagonAtPos.getNextWagon();
            }
            return wagonAtPos;
        }
        return null;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId the id of the wagon you want to find
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon currentWagon = firstWagon;

        if (currentWagon != null) {
            for (int i = 0; i < getNumberOfWagons(); i++) {

                if (currentWagon.getId() == wagonId) {
                    return currentWagon;
                }

                currentWagon = currentWagon.getNextWagon();
            }
        }

        return null;
    }

    /**
     * Determines if the given sequence of wagons can be attached to the train
     * Verfies of the type of wagons match the type of train (Passenger or Freight)
     * Verfies that the capacity of the engine is sufficient to pull the additional wagons
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     * @return whether attaching is possible
     */
    public boolean canAttach(Wagon wagon) {
        final int SPACE_OWN_WAGON = 1;

        if (firstWagon == null) {
            return engine.getMaxWagons() >= wagon.getTailLength() + SPACE_OWN_WAGON &&
                    findWagonById(wagon.getId()) == null;
        }
        return firstWagon.getClass() == wagon.getClass() &&
                engine.getMaxWagons() >= (getNumberOfWagons() + (wagon.getTailLength() + SPACE_OWN_WAGON)) &&
                findWagonById(wagon.getId()) == null;
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {

        if (canAttach(wagon)) {
            wagon.detachFront();

            if (firstWagon == null) {
                firstWagon = wagon;
            } else {
                firstWagon.getLastWagonAttached().attachTail(wagon);
            }

            return true;
        }
        return false;
    }


    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        if (canAttach(wagon)) {
            wagon.detachFront();

            if (firstWagon != null) {
                wagon.getLastWagonAttached().attachTail(firstWagon);
            }

            firstWagon = wagon;
            return true;
        }
        return false;
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given wagon position in the train
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid in this train)
     * if insertion is possible, the head wagon is first detached from its predecessors
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        Wagon wagonAtPos = findWagonAtPosition(position);
        final int FIRST_WAGON_POSITION = 1;

        if (canAttach(wagon) && wagonAtPos != null) {
            wagon.detachFront();
            Wagon head = wagonAtPos.detachFront();
            head.attachTail(wagon);
            wagon.getLastWagonAttached().attachTail(wagonAtPos);
            return true;
        } else if (canAttach(wagon) && position == FIRST_WAGON_POSITION) {
            firstWagon = wagon;
            return true;
        }

        return false;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon you want to move
     * @param toTrain the train you want to attach the wagon to
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        Wagon moveWagon = findWagonById(wagonId);

        if (moveWagon != null && toTrain.canAttach(moveWagon)) {

            if (moveWagon == firstWagon) {
                firstWagon = moveWagon.getNextWagon();
            }

            moveWagon.removeFromSequence();
            toTrain.attachToRear(moveWagon);
            return true;
        }

        return false;
    }

    /**
     * Tries to split this train before the given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position the position you want to split at
     * @param toTrain  the train you want to attach all wagons beginning from position to
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        Wagon wagonAtPos = findWagonAtPosition(position);
        final int FIRST_WAGON_POSITION = 1;

        if (wagonAtPos != null && toTrain.canAttach(wagonAtPos)) {
            wagonAtPos.detachFront();

            if (position == FIRST_WAGON_POSITION) {
                firstWagon = null;
            }

            toTrain.attachToRear(wagonAtPos);
            return true;
        }

        return false;
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        if (firstWagon != null) {
            firstWagon = firstWagon.reverseSequence();
        }
    }

    /**
     * makes a string of all information about the train (locomotive, wagons, number of wagons,
     * origin, destination, number of seats or maximum weight)
     * @return a string containing all information about the train
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(engine.toString());
        Wagon curWagon = firstWagon;

        for (int i = 0; i < getNumberOfWagons(); i++) {
            string.append(curWagon.toString());
            curWagon = curWagon.getNextWagon();
        }

        string.append(" with ").append(getNumberOfWagons()).append(" wagons from ").append(origin).append(" to ").append(destination);

        if (isFreightTrain()) {
            string.append("\nTotal maximum weight: ").append(getTotalMaxWeight());
        }

        if (isPassengerTrain()) {
            string.append("\nTotal number of seats: ").append(getTotalNumberOfSeats());
        }

        return string.toString();
    }
}
