import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.junit.Test;
import org.mockito.Mockito;
import pl.radmit.log.Logger;
import pl.radmit.noIpService.models.NoIpAccount;
import pl.radmit.noIpService.services.NoIpApiService;
import pl.radmit.noIpService.services.NoIpChecker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by rmorawski on 24.08.16.
 */
public class NoIpTest {

    @Test
    public void ifOneErrorServiceCallPrimaryUrl() throws IOException {
        NoIpApiService apiService = Mockito.mock(NoIpApiService.class);
        Logger logger = Mockito.mock(Logger.class);

        Mockito.when(apiService.getMyIpActualFromPrimaryUrl()).thenReturn("1.1.1.1");
        Mockito.when(apiService.getMyIpActualFromSecondaryUrl()).thenReturn("2.2.2.2");
        Mockito.when(logger.getPrintStream()).thenReturn(System.out);

        NoIpChecker ipChecker = new NoIpChecker();
        ipChecker.setApiServiceAndLogger(apiService, logger);
        ipChecker.runOne(true);

        Mockito.verify(apiService, Mockito.times(1)).getMyIpActualFromPrimaryUrl();
        Mockito.verify(apiService, Mockito.times(0)).getMyIpActualFromSecondaryUrl();
    }

    @Test
    public void ifFreeErrorServiceCallSecondaryUrl() throws IOException {
        NoIpApiService apiService = Mockito.mock(NoIpApiService.class);
        Logger logger = Mockito.mock(Logger.class);

        Mockito.when(apiService.getMyIpActualFromPrimaryUrl()).thenThrow(NullPointerException.class);
        Mockito.when(apiService.getMyIpActualFromSecondaryUrl()).thenReturn("2.2.2.2");
        Mockito.when(logger.getPrintStream()).thenReturn(System.out);

        NoIpChecker ipChecker = new NoIpChecker();
        ipChecker.setApiServiceAndLogger(apiService, logger);
        for (int i = 0; i <= 3; i++) {
            ipChecker.runOne(true);
        }

        Mockito.verify(apiService, Mockito.times(3)).getMyIpActualFromPrimaryUrl();
        Mockito.verify(apiService, Mockito.times(1)).getMyIpActualFromSecondaryUrl();
    }

    @Test
    public void ifSixErrorServiceCallAgainPrimaryUrl() throws IOException {
        NoIpApiService apiService = Mockito.mock(NoIpApiService.class);
        Logger logger = Mockito.mock(Logger.class);

        Mockito.when(apiService.getMyIpActualFromPrimaryUrl()).thenThrow(NullPointerException.class);
        Mockito.when(apiService.getMyIpActualFromSecondaryUrl()).thenThrow(NullPointerException.class);
        Mockito.when(logger.getPrintStream()).thenReturn(System.out);

        NoIpChecker ipChecker = new NoIpChecker();
        ipChecker.setApiServiceAndLogger(apiService, logger);
        for (int i = 0; i <= 5; i++) {
            ipChecker.runOne(true);
        }

        Mockito.verify(apiService, Mockito.times(4)).getMyIpActualFromPrimaryUrl();
        Mockito.verify(apiService, Mockito.times(2)).getMyIpActualFromSecondaryUrl();
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
