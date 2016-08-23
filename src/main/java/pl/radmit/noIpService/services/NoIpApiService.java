package pl.radmit.noIpService.services;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import pl.radmit.log.Logger;
import pl.radmit.noIpService.models.IpFile;
import pl.radmit.noIpService.models.NoIpAccount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Radek on 23.08.2016.
 */
public class NoIpApiService {

    private static final String IP_URL_PRIMARY = "";
    private static final String IP_URL_SECONDARY = "";

    private static final String API_URL = "";
    private static final String API_PATH = "";
    private static final String USER_AGENT = "RADMIT Update Client v1.0 radekm87@gmail.com";

    private NoIpAccount account;
    private Logger logger;

    public NoIpApiService(NoIpAccount account, Logger logger) {
        this.account = account;
        this.logger = logger;
    }

    public String getMyIpActualFromPrimaryUrl() throws IOException {
        URL urlIp = new URL("http://checkip.amazonaws.com/");
        return readIpFromWww(urlIp);
    }
    public String getMyIpActualFromSecondaryUrl() throws IOException {
        URL urlIp = new URL("http://ip1.dynupdate.no-ip.com/");
        return readIpFromWww(urlIp);
    }
    public String getLastSavedIpFromFile(String actualIp) {
        IpFile cfgFile = new IpFile();
        String savedIp = cfgFile.readIpFromFile();
        if (savedIp == null || savedIp.isEmpty()) {
            cfgFile.writeIpToFile(actualIp);
        }
        return savedIp;
    }
    public void ifIpChangeThenUpdateIpInNoipAndFile(String oldIp, String newIp) throws IOException {
        if (!newIp.equals(oldIp)) {
            sendNewIpToNoIpServer(oldIp, newIp);
            writeNewIpToCfgFile(newIp);
        }
    }

    private String readIpFromWww(URL urlIp) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(urlIp.openStream()));
        String myIp = br.readLine();

        return myIp;
    }

    private void sendNewIpToNoIpServer(String savedIp, String actualIp) throws MalformedURLException, IOException,
            ClientProtocolException {
        logger.log("Stare ip to: " + savedIp + " a nowe to: " + actualIp
                + " wiec chce zaktualizowac w NOIP");
        URL url = new URL("http://radmit:morawski87@dynupdate.no-ip.com/nic/update?hostname=radmit.noip.me&myip="
                + actualIp);

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url.toString());

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        int resultCode = response.getStatusLine().getStatusCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ": Response Code : " + resultCode);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        logger.log(result.toString());

        if (resultCode != 200 || !(result.toString().contains("nochg") || result.toString().contains("good"))) {
            throw new ClientProtocolException("Otrzymano status błędu z noip.me. Nie aktualizuje informacji w pliku.");
        }
    }

    private void writeNewIpToCfgFile(String myIp) {
        IpFile cfgFile = new IpFile();
        cfgFile.writeIpToFile(myIp);

        logger.log("Zapisalem nowe IP do pliku.");
    }
}
