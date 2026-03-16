import java.util.*;

    enum SpotStatus {
        EMPTY,
        OCCUPIED,
        DELETED
    }

    class ParkingSpot {
        String licensePlate;
        long entryTime;
        SpotStatus status;

        public ParkingSpot() {
            status = SpotStatus.EMPTY;
        }
    }

    public class ParkingLot {

        private ParkingSpot[] table;
        private int capacity;
        private int size;

        private int totalProbes = 0;
        private int totalOperations = 0;

        public ParkingLot(int capacity) {

            this.capacity = capacity;
            this.table = new ParkingSpot[capacity];

            for (int i = 0; i < capacity; i++)
                table[i] = new ParkingSpot();
        }


        private int hash(String licensePlate) {
            return Math.abs(licensePlate.hashCode()) % capacity;
        }


        public int parkVehicle(String licensePlate) {

            int index = hash(licensePlate);
            int probes = 0;

            while (table[index].status == SpotStatus.OCCUPIED) {
                probes++;
                index = (index + 1) % capacity;
            }

            table[index].licensePlate = licensePlate;
            table[index].entryTime = System.currentTimeMillis();
            table[index].status = SpotStatus.OCCUPIED;

            size++;
            totalProbes += probes;
            totalOperations++;

            System.out.println(
                    "Assigned spot #" + index +
                            " (" + probes + " probes)"
            );

            return index;
        }


        public void exitVehicle(String licensePlate) {

            int index = hash(licensePlate);

            while (table[index].status != SpotStatus.EMPTY) {

                if (table[index].status == SpotStatus.OCCUPIED &&
                        table[index].licensePlate.equals(licensePlate)) {

                    long duration =
                            System.currentTimeMillis() - table[index].entryTime;

                    double hours = duration / (1000.0 * 60 * 60);

                    double fee = Math.ceil(hours * 5); // $5 per hour

                    table[index].status = SpotStatus.DELETED;
                    size--;

                    System.out.println(
                            "Spot #" + index +
                                    " freed, Duration: " +
                                    String.format("%.2f", hours) +
                                    " hours, Fee: $" + fee
                    );

                    return;
                }

                index = (index + 1) % capacity;
            }

            System.out.println("Vehicle not found");
        }


        public int findNearestSpot() {

            for (int i = 0; i < capacity; i++) {

                if (table[i].status == SpotStatus.EMPTY ||
                        table[i].status == SpotStatus.DELETED) {

                    return i;
                }
            }

            return -1;
        }


        public void getStatistics() {

            double occupancy = (size * 100.0) / capacity;

            double avgProbes =
                    totalOperations == 0 ? 0 :
                            (double) totalProbes / totalOperations;

            System.out.println("\n--- Parking Statistics ---");
            System.out.println("Occupancy: " +
                    String.format("%.2f", occupancy) + "%");

            System.out.println("Avg Probes: " +
                    String.format("%.2f", avgProbes));
        }


        public static void main(String[] args) {

            ParkingLot lot = new ParkingLot(500);

            lot.parkVehicle("ABC-1234");
            lot.parkVehicle("ABC-1235");
            lot.parkVehicle("XYZ-9999");

            lot.exitVehicle("ABC-1234");

            lot.getStatistics();
        }
    }

