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
import java.util.Base64;

/**
 * Created by Radek on 23.08.2016.
 */
public class NoIpApiService {

    private static final String IP_URL_PRIMARY = "http://checkip.amazonaws.com/";
    private static final String IP_URL_SECONDARY = "http://ip1.dynupdate.no-ip.com/";

    private static final String API_URL = "http://dynupdate.no-ip.com/nic/update?hostname=";
    private static final String API_PATH = "&myip=";
    private static final String USER_AGENT = "RADMIT Update Client v1.0 radekm87@gmail.com";

    private NoIpAccount account;
    private Logger logger;

    public NoIpApiService(NoIpAccount account, Logger logger) {
        this.account = account;
        this.logger = logger;
    }

    public String getMyIpActualFromPrimaryUrl() throws IOException {
        URL urlIp = new URL(IP_URL_PRIMARY);
        return readIpFromWww(urlIp);
    }

    public String getMyIpActualFromSecondaryUrl() throws IOException {
        URL urlIp = new URL(IP_URL_SECONDARY);
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

    /*
    Uzywane do testow
     */
    public void sendNewIpToNoIpServer(String savedIp, String actualIp, HttpClient client) throws IOException {
        logger.log("Stare ip to: " + savedIp + " a nowe to: " + actualIp
                + " wiec chce zaktualizowac w NOIP");


        HttpGet request = prepareHttpGetRequest(actualIp);
        HttpResponse response = client.execute(request);

        int resultCode = response.getStatusLine().getStatusCode();

        logger.log("Response Code : " + resultCode);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        logger.log("Result: " + result.toString());

        if (resultCode != 200 || !(result.toString().contains("nochg") || result.toString().contains("good"))) {
            throw new ClientProtocolException("Otrzymano status błędu z noip.me. Nie aktualizuje informacji w pliku.");
        }
    }

    private HttpGet prepareHttpGetRequest(String actualIp) throws MalformedURLException {
        URL url = new URL(API_URL + account.getHostname() + API_PATH + actualIp);

        HttpGet request = new HttpGet(url.toString());

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        String base64AuthorizationToEncode = account.getUsername() + ":" + account.getPassword();
        String base64AuthorizationEncoded = "Basic " + Base64.getEncoder().encodeToString(base64AuthorizationToEncode.getBytes());

        request.addHeader("Authorization", base64AuthorizationEncoded);

        logger.log("Preparing and ready to send 'GET' request to URL : " + url);
        return request;
    }

    private void sendNewIpToNoIpServer(String savedIp, String actualIp) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        sendNewIpToNoIpServer(savedIp, actualIp, client);
    }

    public void writeNewIpToCfgFile(String myIp) {
        IpFile cfgFile = new IpFile();
        cfgFile.writeIpToFile(myIp);

        logger.log("Zapisalem nowe IP do pliku.");
    }
}
