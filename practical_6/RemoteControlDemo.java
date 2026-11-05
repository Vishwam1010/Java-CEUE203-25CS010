interface Switchable {
    String name();

    void on();

    void off();

    default void toggle() {
        off();
        on();
    }
}

class Fan implements Switchable {
    @Override
    public String name() {
        return "Fan";
    }

    @Override
    public void on() {
        System.out.println("Fan is on");
    }

    @Override
    public void off() {
        System.out.println("Fan is off");
    }
}

class Light implements Switchable {
    @Override
    public String name() {
        return "Light";
    }

    @Override
    public void on() {
        System.out.println("Light is on");
    }

    @Override
    public void off() {
        System.out.println("Light is off");
    }
}

@FunctionalInterface
interface SwitchPolicy {
    boolean maySwitchOn(Switchable device, int hour);
}

public class RemoteControlDemo {
    public static void main(String[] args) {
        Switchable[] devices = {
            new Fan(),
            new Light()
        };

        System.out.println("Toggling devices:");
        for (Switchable device : devices) {
            System.out.println(device.name() + ":");
            device.toggle();
        }

        // Keep this anonymous class to demonstrate the required syntax.
        @SuppressWarnings("Convert2Lambda")
        SwitchPolicy daytimePolicy = new SwitchPolicy() {
            @Override
            public boolean maySwitchOn(Switchable device, int hour) {
                return hour >= 8 && hour <= 20;
            }
        };

        // Lambda: allow the fan at any hour and the light before 23:00.
        SwitchPolicy eveningPolicy = (device, hour) ->
                device instanceof Fan || hour < 23;

        int hour = 21;

        System.out.println("\nAnonymous policy at hour " + hour + ":");
        applyPolicy(devices, hour, daytimePolicy);

        System.out.println("\nLambda policy at hour " + hour + ":");
        applyPolicy(devices, hour, eveningPolicy);
    }

    private static void applyPolicy(
            Switchable[] devices, int hour, SwitchPolicy policy) {
        for (Switchable device : devices) {
            if (policy.maySwitchOn(device, hour)) {
                System.out.println(device.name() + " may switch on.");
                device.on();
            } else {
                System.out.println(device.name() + " may not switch on.");
                device.off();
            }
        }
    }
}