public class Thermostat {
    private static final int MIN = 16;
    private static final int MAX = 30;
    private static final int DEFAULT_TEMPERATURE = 22;

    private final String location;
    private int temperature;
    private static int activeCount = 0;

    public Thermostat(String location, int startTemp) {
        this.location = location;
        this.temperature = (startTemp >= MIN && startTemp <= MAX)
                ? startTemp
                : DEFAULT_TEMPERATURE;
        activeCount++;
    }

    public Thermostat(String location) {
        this(location, DEFAULT_TEMPERATURE);
    }

    public void raise() {
        if (temperature < MAX) {
            temperature++;
        } else {
            System.out.println("Already at maximum (" + MAX + ")");
        }
    }

    public void lower() {
        if (temperature > MIN) {
            temperature--;
        } else {
            System.out.println("Already at minimum (" + MIN + ")");
        }
    }

    public String getLocation() {
        return location;
    }

    public int getTemperature() {
        return temperature;
    }

    public static int getActiveCount() {
        return activeCount;
    }

    public static void main(String[] args) {
        Thermostat livingRoom = new Thermostat("Living room", 22);
        Thermostat bedroom = new Thermostat("Bedroom");

        // Raise both thermostats 10 times.
        for (int i = 0; i < 10; i++) {
            livingRoom.raise();
            bedroom.raise();
            printTemperature(livingRoom);
            printTemperature(bedroom);
        }

        // Lower both thermostats 20 times.
        for (int i = 0; i < 20; i++) {
            livingRoom.lower();
            bedroom.lower();
            printTemperature(livingRoom);
            printTemperature(bedroom);
        }

        System.out.println("Active thermostats: " + Thermostat.getActiveCount());
    }

    private static void printTemperature(Thermostat thermostat) {
        System.out.println(thermostat.getLocation() + ": "
                + thermostat.getTemperature());
    }
}