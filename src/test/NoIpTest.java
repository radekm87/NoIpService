import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import pl.radmit.log.Logger;
import pl.radmit.noIpService.models.NoIpAccount;
import pl.radmit.noIpService.services.NoIpApiService;
import pl.radmit.noIpService.services.NoIpChecker;

import java.io.IOException;

/**
 * Created by rmorawski on 24.08.16.
 */
public class NoIpTest {

    @Test
    public void getMyIpFromPrimaryUrl() throws IOException {
        NoIpApiService apiService = Mockito.mock(NoIpApiService.class);
        Logger logger = Mockito.mock(Logger.class);

        Mockito.when(apiService.getMyIpActualFromPrimaryUrl()).thenReturn("8.8.8.8");
        Mockito.when(logger.getPrintStream()).thenReturn(System.out);

        NoIpChecker ipChecker = new NoIpChecker();
        ipChecker.setApiServiceAndLogger(apiService, logger);
        ipChecker.runOne(true);

        Mockito.when(apiService.getMyIpActualFromPrimaryUrl()).thenThrow(NullPointerException.class);
        ipChecker.runOne(true);
        ipChecker.runOne(true);
        ipChecker.runOne(true);
        ipChecker.runOne(true);
    }
}
