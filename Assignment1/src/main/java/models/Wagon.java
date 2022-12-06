package models;

/**
 *
 */
public abstract class Wagon {
    protected int id;               // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon

    public Wagon(int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        return nextWagon != null;
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        return previousWagon != null;
    }

    /**
     * Returns the last wagon attached to it, if there are no wagons attached to it then this wagon is the last wagon.
     *
     * @return the wagon
     */
    public Wagon getLastWagonAttached() {
        Wagon lastWagon = this;

        while (lastWagon.getNextWagon() != null) {
            lastWagon = lastWagon.getNextWagon();
        }

        return lastWagon;
    }

    /**
     * @return the length of the tail of wagons towards the end of the sequence
     * excluding this wagon itself.
     */
    public int getTailLength() {
        int numberOfWagons = 0;
        Wagon lastWagon = this;

        while (lastWagon.hasNextWagon()) {
            lastWagon = lastWagon.getNextWagon();
            numberOfWagons++;
        }

        return numberOfWagons;
    }

    /**
     * Attaches the tail wagon behind this wagon, if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     *
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     */
    public void attachTail(Wagon tail) {

        if (hasNextWagon()) {
            throw new IllegalStateException("Wagon " + this + " already has wagon " + this.nextWagon + " appended to it so " + tail + " cannot be appended.");
        }
        if (tail.hasPreviousWagon()) {
            throw new IllegalStateException("Wagon " + this + " already has wagon" + tail.previousWagon + " in front of it so " + tail + " cannot be appended.");
        }

        nextWagon = tail;
        tail.previousWagon = this;
    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     *
     * @return the first wagon of the tail that has been detached
     * or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {
        Wagon tail = nextWagon;

        if (tail != null) {
            nextWagon = null;
            tail.previousWagon = null;
        }

        return tail;
    }

    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     *
     * @return the former previousWagon that has been detached from,
     * or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        Wagon front = previousWagon;

        if (front != null) {
            front.nextWagon = null;
            previousWagon = null;
        }

        return front;
    }

    /**
     * Replaces the tail of the <code>front</code> wagon by this wagon
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon from its predecessor,
     * and the <code>front</code> wagon from its current tail.
     *
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        this.detachFront();
        front.detachTail();
        front.attachTail(this);

    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if it exists.
     */
    public void removeFromSequence() {
        Wagon formerNext = nextWagon;
        Wagon formerPrev = previousWagon;
        detachFront();
        detachTail();

        if (formerPrev != null && formerNext != null) {
            formerPrev.attachTail(formerNext);
        }
    }

    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon lastWagon = getLastWagonAttached();
        Wagon currWagon = this;
        int tailLength = this.getTailLength();

        for (int i = 0; i < tailLength; i++) {
            Wagon nextWagon = currWagon.getNextWagon();
            currWagon.removeFromSequence();
            Wagon tail = lastWagon.detachTail();
            lastWagon.attachTail(currWagon);

            if (tail != null) {
                currWagon.attachTail(tail);
            }

            currWagon = nextWagon;
        }

        return lastWagon;
    }

    /**
     * makes a string of the wagon displaying the id
     * @return the word wagon and the id of the wagon
     */
    @Override
    public String toString() {
        return "[Wagon-" + id + "]";
    }
}
