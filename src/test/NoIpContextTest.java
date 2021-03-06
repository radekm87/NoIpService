import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import pl.radmit.log.Logger;
import pl.radmit.noIpService.models.NoIpAccount;
import pl.radmit.noIpService.services.NoIpApiService;

import java.io.IOException;

public class NoIpContextTest {

    @Test
    public void getMyIpFromPrimaryUrl() throws IOException {
        NoIpAccount noIpAccount = Mockito.mock(NoIpAccount.class);
        Logger logger = Mockito.mock(Logger.class);

        NoIpApiService apiService = new NoIpApiService(noIpAccount, logger);
        String myIp = apiService.getMyIpActualFromPrimaryUrl();

        Assert.assertTrue(!myIp.isEmpty());
    }

    @Test
    public void getMyIpFromSecondaryUrl() throws IOException {
        NoIpAccount noIpAccount = Mockito.mock(NoIpAccount.class);
        Logger logger = Mockito.mock(Logger.class);

        NoIpApiService apiService = new NoIpApiService(noIpAccount, logger);
        String myIp = apiService.getMyIpActualFromSecondaryUrl();

        Assert.assertTrue(!myIp.isEmpty());
    }

    @Test
    public void getLastSavedIpFromFile() throws IOException {
        NoIpAccount noIpAccount = Mockito.mock(NoIpAccount.class);
        Logger logger = Mockito.mock(Logger.class);

        NoIpApiService apiService = new NoIpApiService(noIpAccount, logger);
        String myIp = apiService.getLastSavedIpFromFile("8.8.8.8");

        Assert.assertTrue(!myIp.isEmpty());
    }

    @Test
    public void writeNewIpToCfgFile() throws IOException {
        NoIpAccount noIpAccount = Mockito.mock(NoIpAccount.class);
        Logger logger = Mockito.mock(Logger.class);

        NoIpApiService apiService = new NoIpApiService(noIpAccount, logger);
        String actualIp = apiService.getLastSavedIpFromFile("8.8.8.8");
        apiService.writeNewIpToCfgFile("1.1.1.1");
        String reReadIp = apiService.getLastSavedIpFromFile("8.8.8.8");
        Assert.assertTrue(reReadIp.equals("1.1.1.1"));

        apiService.writeNewIpToCfgFile(actualIp);
    }
}