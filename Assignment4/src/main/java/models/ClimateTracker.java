package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.CertificateParsingException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Double.*;

public class ClimateTracker {
    private final String MEASUREMENTS_FILE_PATTERN = ".*\\.txt";

    private Map<Integer, Station> stations;        // all available weather stations organised by Station Number (STN)

    public Set<Station> getStations() {
        return Set.copyOf(this.stations.values());
    }

    public Station findStationById(int stn) {
        for (Map.Entry<Integer, Station> station : stations.entrySet()) {
            if (station.getKey().equals(stn)) return station.getValue();
        }

        return null;
    }

    public ClimateTracker() {
        this.stations = new HashMap<>();
    }


    /**
     * calculates for each station how many Measurement instances have been registered
     *
     * @return
     */
    public Map<Station, Integer> numberOfMeasurementsByStation() {
        return this.stations.values().stream()
                .collect(Collectors.toMap(station -> station, station -> station.getMeasurements().size()));
    }



    /**
     * calculates for each station the date of the first day of its measurements
     * stations without measurements shall be excluded from the result
     *
     * @return
     */
    public Map<Station, LocalDate> firstDayOfMeasurementByStation() {

        return stations.entrySet().stream()
                .filter(station -> station.getValue().firstDayOfMeasurement().isPresent())
                .collect(Collectors
                        .toMap(Map.Entry::getValue,
                                station -> station.getValue().firstDayOfMeasurement().get(), (a, b) -> b));
    }

    /**
     * calculates for each station how many valid values it has available for the measurement quantity
     * that can be accessed by the mapper.
     * invalid values are registered as Double.NaN. They originate from empty or corrupt data in the source file
     *
     * @param mapper a getter method that accesses the selected quantity from a Measurement instance
     * @return
     */
    public Map<Station, Integer> numberOfValidValuesByStation(Function<Measurement, Double> mapper) {
        Map<Station, Integer> regiMap = new HashMap<>();

        for (Map.Entry<Integer, Station> station : stations.entrySet()) {
            regiMap.put(station.getValue(), station.getValue().numValidValues(mapper));
        }

        return regiMap;
    }

    /**
     * Calculates for each calendar year in the dataset the average daily temperatures
     * across all days in the year and all stations in this tracker
     * (invalid values shall be excluded from the averaging)
     *
     * @return a map(Y,T) that provides for each year Y the average temperature T of that year
     */
    public Map<Integer, Double> annualAverageTemperatureTrend() {
        Map<Integer, List<Measurement>> map = stations.values().stream()
                .map(Station::getMeasurements)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(measurement -> measurement.getDate().getYear()));

        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, integerListEntry -> integerListEntry.getValue()
                        .stream()
                        .mapToDouble(Measurement::getAverageTemperature)
                        .filter(measurement -> !Double.isNaN(measurement))
                        .average()
                        .orElse(NaN)));

    }

    private Set<Integer> getYearsMeasured() {
        Set<Integer> years = new HashSet<>();

        for (Station station : stations.values()) {
            years.addAll(station.getYearsMeasured());
        }

        return years;
    }

    /**
     * Calculates for each calendar year in the dataset the maximum of the selected daily quantity
     * across all days in the year and all stations in this tracker
     * (invalid values shall be excluded from the maximum aggregation)
     * (this method can be reused for maximum aggregation of different quantities in the Measurement data
     *
     * @param mapper a getter function on the Measurement class
     *               that selects the appropriate value for the maximum aggregation procedure
     * @return a map(Y,Q) that provides for each year Y the maximum value Q of the specified quantity
     */
    public Map<Integer, Double> annualMaximumTrend(Function<Measurement, Double> mapper) {
        Map<Integer, Double> regiMap = new HashMap<>();

        for (Integer year : getYearsMeasured()) {
            double maxVal = this.getStations().stream().map(Station::getMeasurements)
                    .flatMap(Collection::stream)
                    .filter(measurement -> measurement.getDate().getYear() == year)
                    .map(mapper)
                    .filter(measurement -> !measurement.isNaN())
                    .mapToDouble(Double::valueOf)
                    .max()
                    .orElse(NaN);

            if (!Double.isNaN(maxVal)) regiMap.put(year, maxVal);

        }

        return regiMap;
    }

    /**
     * Calculates for each of the 12 calendar months the average daily hours of sunshine
     * across all years and all stations
     * The graph of these numbers can be found in touristic brochures.
     * (invalid values shall be excluded from the averaging)
     *
     * @return a map(M,SQ) that provides for each month M the average daily sunshine hours SQ across all times
     */
    public Map<Month, Double> allTimeAverageDailySolarByMonth() {
        Map<Month, Double> map = new TreeMap<>();
        List<Month> months = new ArrayList<>();
        for (Station station : stations.values()) {
            for (Month m : station.getMonthsMeasured()) if (!months.contains(m)) months.add(m);
        }

        for (Month month : months) {
            double total = 0;
            int nrOfm = 0;
            for (Station station : stations.values()) {
                total += station.getMeasurements().stream()
                        .filter(measurement -> measurement.getDate().getMonth() == month)
                        .filter(measurement -> !Double.isNaN(measurement.getSolarHours()))
                        .mapToDouble(Measurement::getSolarHours).sum();
                nrOfm += station.getMeasurements().stream()
                        .filter(measurement -> measurement.getDate().getMonth() == month)
                        .filter(measurement -> !Double.isNaN(measurement.getSolarHours()))
                        .count();
            }
            map.put(month, total / nrOfm);
        }

        return map;
    }

    /**
     * calculate the coldest year of all times.
     * The coldest year is defined as the year with the lowest total sum of daily minimum temperatures below zero
     * accumulated across all stations
     * I.e. any daily value of a minimum temperature at a station that is above zero should not be included in its sum for the year
     * The lowest yearsum of negative minimum temperatures indicates the coldest year.
     *
     * @return the coldest year (a number between 1900 and 2099)
     * return -1 if no valid minimum temperature measurements are available
     */
    public int coldestYear() {
        int coldYear = -1;
        double startTemp = 0;

        for (Integer year : getYearsMeasured()) {
            double yearTemp = this.getStations().stream().map(Station::getMeasurements)
                    .flatMap(Collection::stream)
                    .filter(measurement -> measurement.getDate().getYear() == year)
                    .map(Measurement::getMinTemperature)
                    .filter(measurement -> measurement < 0)
                    .mapToDouble(Double::valueOf)
                    .sum();

            if (yearTemp < startTemp) {
                coldYear = year;
                startTemp = yearTemp;
            }
        }
        return coldYear;
    }

    /**
     * imports all station and measurement information
     *
     * @param folderPath
     */
    public void importClimateDataFromVault(String folderPath) {
        this.stations.clear();

        // load all stations from the text file
        importItemsFromFile(this.stations,
                folderPath + "/stations.txt", null,
                Station::fromLine, Station::getStn);

        // load all measurements from the folder
        importMeasurementsFromVault(folderPath + "/measurements");
    }

    /**
     * traverses the purchases vault recursively and processes every data file that it finds
     *
     * @param filePath
     */
    public void importMeasurementsFromVault(String filePath) {

        File file = new File(filePath);

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            //  retrieve a list of all files and sub folders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            // merge all purchases of all files and sub folders from the filesInDirectory list, recursively.
            for (File f : filesInDirectory) {
                this.importMeasurementsFromVault(f.getAbsolutePath());
            }
        } else if (file.getName().matches(MEASUREMENTS_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw purchase files
            // merge the content of this file into this.purchases
            this.importMeasurementsFromFile(file.getAbsolutePath());
        }
    }

    /**
     * imports a collection of items from a text file which provides one line for each item
     *
     * @param items     the list to which imported items shall be added
     * @param filePath  the file path of the source text file
     * @param converter a function that can convert a text line into a new item instance
     * @param <E>       the (generic) type of each item
     */
    private static <K, E> void importItemsFromFile(Map<K, E> items, String filePath, String headerPrefix,
                                                   Function<String, E> converter, Function<E, K> mapper) {
        int originalNumItems = items.size();

        Scanner scanner = createFileScanner(filePath);

        // skip lines until the header is hit
        while (headerPrefix != null && scanner.hasNext()) {
            // import another line of the header
            String line = scanner.nextLine();

            if (line.startsWith(headerPrefix)) headerPrefix = null;
        }

        //  read all remaining source lines from the scanner,
        //  convert each line to an item of type E and
        //  and add each item to the map
        int newCount = 0;
        while (scanner.hasNext()) {
            // input another line
            String line = scanner.nextLine();

            // convert the line into an instance of E
            E newItem = converter.apply(line);

            //System.out.printf("extracted %s from line: %s\n", newItem, line);

            //  put the item into the map after successful conversion,
            //  using the mapper to determine the key
            //  do not overwrite existing items in the map
            if (newItem != null) {
                items.putIfAbsent(mapper.apply(newItem), newItem);
                newCount++;
            }
        }

        if (items.size() < originalNumItems + newCount) {
            throw new InputMismatchException(String.format("Duplicate items found in file %s", filePath));
        }

        //System.out.printf("Imported %d items from %s.\n", items.size() - originalNumItems, filePath);
    }

    /**
     * imports another batch of raw purchase data from the filePath text file
     * and merges the purchase amounts with the earlier imported and accumulated collection in this.purchases
     *
     * @param filePath
     */
    private void importMeasurementsFromFile(String filePath) {

        // create a temporary map to import the measurements, organised by date
        Map<LocalDate, Measurement> newMeasurementsByDate = new HashMap<>();

        // import all measurements from the specified file into the newMeasurementsByDate map
        importItemsFromFile(newMeasurementsByDate, filePath, "# STN",
                s -> Measurement.fromLine(s, this.stations), Measurement::getDate);

        //  add the measurements to their station
        //  (all measurements from the same file should belong to the same station)
        if (newMeasurementsByDate.size() > 0) {
            Station station = newMeasurementsByDate.values().stream().findAny().get().getStation();
            // add the newMeasurements to the map in the station
            int numAdded = station.addMeasurements(newMeasurementsByDate.values());
            if (numAdded != newMeasurementsByDate.size()) {
                throw new InputMismatchException(String.format("Some items in file %s could not be added", filePath));
            }
        }
    }

    /**
     * helper method to create a scanner on a file an handle the exception
     *
     * @param filePath
     * @return
     */
    private static Scanner createFileScanner(String filePath) {
        try {
            return new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + filePath);
        }
    }
}
