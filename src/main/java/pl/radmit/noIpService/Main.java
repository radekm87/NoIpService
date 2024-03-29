package pl.radmit.noIpService;

import pl.radmit.noIpService.services.NoIpChecker;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Start głównego procesu v1.2...");

        Thread noIpServiceThread = new Thread(new NoIpChecker());
        noIpServiceThread.start();
        noIpServiceThread.join();

        System.out.println("Zamykam aplikację - wątek zakończony...");
    }
}
