import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import pl.radmit.log.Logger;
import pl.radmit.noIpService.models.NoIpAccount;
import pl.radmit.noIpService.services.NoIpApiService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    @Test
    public void sendNewIpToNoIpServer() throws IOException {
        NoIpAccount noIpAccount = Mockito.mock(NoIpAccount.class);
        Logger logger = Mockito.mock(Logger.class);

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);

        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream("good 1.1.1.1".getBytes(StandardCharsets.UTF_8)));


        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);


        NoIpApiService apiService = new NoIpApiService(noIpAccount, logger);
        apiService.sendNewIpToNoIpServer("8.8.8.8", "1.1.1.1", httpClient);
    }

    @Test(expected = ClientProtocolException.class)
    public void badResponseCodeFromNoip() throws IOException {
        NoIpAccount noIpAccount = Mockito.mock(NoIpAccount.class);
        Logger logger = Mockito.mock(Logger.class);

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(404);

        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream("good 1.1.1.1".getBytes(StandardCharsets.UTF_8)));


        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);

        NoIpApiService apiService = new NoIpApiService(noIpAccount, logger);
        apiService.sendNewIpToNoIpServer("8.8.8.8", "1.1.1.1", httpClient);
    }

}