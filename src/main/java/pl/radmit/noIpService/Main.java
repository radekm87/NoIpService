package pl.radmit.noIpService;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start głównego procesu...");

        Thread noIpServiceThread = new Thread(new NoIpChecker());
        noIpServiceThread.start();
        noIpServiceThread.join();

        System.out.println("Zamykam aplikację - wątek zakończony...");
    }
}
