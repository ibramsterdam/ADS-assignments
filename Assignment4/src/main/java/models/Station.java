package models;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Station {
    private final int stn;
    private final String name;
    private NavigableMap<LocalDate, Measurement> measurements;

    public Station(int id, String name) {
        this.stn = id;
        this.name = name;
        this.measurements = new TreeMap<>();
    }

    public Collection<Measurement> getMeasurements() {
        return measurements.values();
    }

    public int getStn() {
        return stn;
    }

    public String getName() {
        return name;
    }

    /**
     * import station number and name from a text line
     *
     * @param textLine
     * @return a new Station instance for this data
     * or null if the data format does not comply
     */
    public static Station fromLine(String textLine) {
        String[] fields = textLine.split(",");
        if (fields.length < 2) return null;
        try {
            return new Station(Integer.parseInt(fields[0].trim()), fields[1].trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Add a collection of new measurements to this station.
     * Measurements that are not related to this station
     * and measurements with a duplicate date shall be ignored and not added
     *
     * @param newMeasurements
     * @return the nett number of measurements which have been added.
     */
    public int addMeasurements(Collection<Measurement> newMeasurements) {
        int oldSize = this.getMeasurements().size();

        for (Measurement measurement : newMeasurements) {
            LocalDate date = measurement.getDate();
            if (measurement.getStation() == this && measurements.navigableKeySet().stream().noneMatch(m -> m.isEqual(date))) {
                this.measurements.put(date, measurement);
            }
        }

        return this.getMeasurements().size() - oldSize;
    }

    /**
     * calculates the all-time maximum temperature for this station
     *
     * @return the maximum temperature ever measured at this station
     * returns Double.NaN when no valid measurements are available
     */
    public double allTimeMaxTemperature() {
        Optional<Measurement> result = this.measurements.values().stream().max(Comparator.comparing(Measurement::getMaxTemperature));
        return result.map(Measurement::getMaxTemperature).orElse(Double.NaN);
    }

    /**
     * @return the date of the first day of a measurement for this station
     * returns Optional.empty() if no measurements are available
     */
    public Optional<LocalDate> firstDayOfMeasurement() {
        try {
            return Optional.of(this.measurements.firstKey());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    /**
     * calculates the number of valid values of the data field that is specified by the mapper
     * invalid or empty values should be are represented by Double.NaN
     * this method can be used to check on different types of measurements each with their own mapper
     *
     * @param mapper the getter method of the data field to be checked.
     * @return the number of valid values found
     */
    public int numValidValues(Function<Measurement, Double> mapper) {
        return (int) measurements.values().stream().map(mapper).filter(value -> !value.equals(Double.NaN)).count();
    }

    /**
     * calculates the total precipitation at this station
     * across the time period between startDate and endDate (inclusive)
     *
     * @param startDate the start date of the period of accumulation (inclusive)
     * @param endDate   the end date of the period of accumulation (inclusive)
     * @return the total precipitation value across the period
     * 0.0 if no measurements have been made in this period.
     */
    public double totalPrecipitationBetween(LocalDate startDate, LocalDate endDate) {
        return this.getMeasurementsBetweenInclusive(startDate, endDate).stream().mapToDouble(measurement -> measurement.getPrecipitation() > 0 ? measurement.getPrecipitation() : 0).sum();
    }

    /**
     * calculates the average of all valid measurements of the quantity selected by the mapper function
     * across the time period between startDate and endDate (inclusive)
     *
     * @param startDate the start date of the period of averaging (inclusive)
     * @param endDate   the end date of the period of averaging (inclusive)
     * @param mapper    a getter method that obtains the double value from a measurement instance to be averaged
     * @return the average of all valid values of the selected quantity across the period
     * Double.NaN if no valid measurements are available from this period.
     */
    public double averageBetween(LocalDate startDate, LocalDate endDate, Function<Measurement, Double> mapper) {
        List<Measurement> average = this.getMeasurementsBetweenInclusive(startDate, endDate).stream()
                .filter(measurement -> !Double.isNaN(mapper.apply(measurement))).collect(Collectors.toList());

        if (average.size() > 0) return average.stream().collect(Collectors.averagingDouble(mapper::apply));
        return Double.NaN;
    }

    private List<Measurement> getMeasurementsBetweenInclusive(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>(this.measurements.subMap(startDate, true, endDate, true).values());
    }

    public Set<Integer> getYearsMeasured() {
        return measurements.values().stream().map(measurement -> measurement.getDate().getYear()).collect(Collectors.toSet());
    }

    public Set<Month> getMonthsMeasured() {
        return measurements.values().stream().map(measurement -> measurement.getDate().getMonth()).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return this.stn + "/" + this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(name, station.name) && Objects.equals(measurements, station.measurements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stn);
    }
}
