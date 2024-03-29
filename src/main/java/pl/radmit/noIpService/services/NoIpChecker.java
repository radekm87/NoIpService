package pl.radmit.noIpService.services;

import pl.radmit.log.Logger;
import pl.radmit.log.NoIpLoggerConfiguratorImpl;
import pl.radmit.noIpService.models.NoIpAccount;
import pl.radmit.noIpService.models.NoIpConfiguration;

import java.io.IOException;

public class NoIpChecker implements Runnable {
    private boolean isRun;
    private Logger logger;

    // co ile czasu ma badac aktualne IP
    private int TIME_SLEEP;
    // zmienna sterujaca logowanie bledow do pliku - jesli kilka razy ten sam blad to po co pisac?
    private boolean isError;
    // zmienna sterujaca do jakiego serwisu pytamy o ip
    private int howException;

    private NoIpApiService apiService;

    public void setApiServiceAndLogger(NoIpApiService apiService, Logger loger) {
        this.apiService = apiService;
        this.logger = loger;
    }

    @Override
    public void run() {
        configureThread();

        try {
            while (isRun) {
                runOne();
            }
        } catch (InterruptedException e) {
            logger.log(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            logger.log("   ***** Exception i wyszło poza pętle - zamykam wątek!!!");
        }

    }

    public void runOne() throws InterruptedException {
        try {

            String actualIp = getMyCurrentIp();

            String savedIp = apiService.getLastSavedIpFromFile(actualIp); // aktualne ip na wypadek pierwszego odczytu z pliku
            apiService.ifIpChangeThenUpdateIpInNoipAndFile(savedIp, actualIp);

            // byl error, ale juz jest ok to resetujemy ustawienia
            if (isError) {
                isError = false;
                howException = 0;
            }
        } catch (Exception e) {
            howException++;
            if (!isError) {
                logger.log("******** Wystapil błąd!!!!!!" + e.getMessage());
                e.printStackTrace(logger.getPrintStream());
            }
            isError = true;
        } finally {
            Thread.sleep(TIME_SLEEP);
        }
    }

    private String getMyCurrentIp() throws IOException {
        String actualIp = null;
        //Jesli wystapil dwa razy wyjatek to za trzecim pobieramy z innego serwisu, jesli wiecej jak 5 to znow wracamy
        if (howException > 2 && howException < 5) {
            try {
                actualIp = apiService.getMyIpActualFromSecondaryUrl();
            } catch (Exception e) {
                logger.log("******** Wystąpił błąd METODA ALTERNATYWNA!!!!!!" + e.getMessage());
                e.printStackTrace(logger.getPrintStream());
                throw e;
            }
            howException = 0; // resetujemy licznik
        } else if (howException >= 5) {
            logger.log("Nie uzyskano IP z serwisu alternatywnego wiec wracam do serwisu glownego: ");
            howException = 0;
            isError = false;
            actualIp = apiService.getMyIpActualFromPrimaryUrl();
        } else {
            actualIp = apiService.getMyIpActualFromPrimaryUrl();
        }
        return actualIp;
    }

    private void configureThread() {
        this.logger = new Logger(new NoIpLoggerConfiguratorImpl());
        this.TIME_SLEEP = 240000; // 4min // 300000; // 5 minut
        this.isRun = true;
        this.isError = false;
        this.howException = 0;

        NoIpConfiguration configuration = new NoIpConfiguration();
        if (!configuration.isValid()) {
            throw new NullPointerException("Plik konfiguracyjny nieprawidłowy. Zamykam wątek.");
        }

        NoIpAccount account = new NoIpAccount(configuration.getUsername(), configuration.getPassword(), configuration.getHostname());
        this.apiService = new NoIpApiService(account, logger);

        logger.log("Wątek NoIpChecker skonfigurowany poprawnie.");
    }
}
