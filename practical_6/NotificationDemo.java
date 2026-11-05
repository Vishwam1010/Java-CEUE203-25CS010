@FunctionalInterface
interface Notifier {
    void send(String message);
}

interface Urgent {
    // Marker interface: it has no methods.
}

public class NotificationDemo {
    public static void main(String[] args) {
        Notifier emailSender = (Notifier & Urgent) message ->
                System.out.println("Email: " + message);

        Notifier smsSender = message ->
                System.out.println("SMS: " + message);

        Notifier backupEmailSender = (Notifier & Urgent) message ->
                System.out.println("Backup email: " + message);

        Notifier[] senders = {
            emailSender,
            smsSender,
            backupEmailSender
        };

        broadcast(senders, "Your appointment is confirmed.");
    }

    private static void broadcast(Notifier[] senders, String message) {
        for (Notifier sender : senders) {
            sender.send(message);

            if (sender instanceof Urgent) {
                sender.send(message);
            }
        }
    }
}