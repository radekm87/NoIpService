import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;

public class Main
{
	private static final int TIME_SLEEP = 240000; // 4min // 300000; // 5 minut
	private static boolean isRun = true;
	private static String myIp = "";
	private static PrintStream log = getErrorLoggerPrintStream();

	public static PrintStream getErrorLoggerPrintStream()
	{
		try
		{
			PrintStream s = new PrintStream(new File("NoIpLogFile"
					+ new SimpleDateFormat("yyyyMMdd_HHmm").format(Calendar.getInstance().getTime())));
			return s;
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception
	{
		System.setErr(log);
		System.setOut(log);
		log.println(DateTime.now().toString("yyyy-MM-dd HH:mm") + ": Start procesu...");

		boolean isError = false;
		int howException = 0;

		while (isRun)
		{
			try
			{
				Thread.sleep(TIME_SLEEP);

				//Jesli wystapil dwa razy wyjatek to za trzecim pobieramy z innego serwisu
				if (howException > 2)
				{
					try
					{
						getMyIpFromAnotherWWW();
					} catch (Exception e)
					{
						log.println("******** Wystapil b³¹d METODA ALTERNATYWNA!!!!!!" + e.getMessage());
						log.println("Zdarzenie z godziny: " + DateTime.now().toString("yyyy-MM-dd HH:mm"));
						e.printStackTrace(log);
						throw e;
					}
					howException = 0; // resetujemy licznik
				} else
				{
					getMyIpFromAmazon();
				}

				String savedIp = readIpFromCfgFile();

				if (!myIp.equals(savedIp))
				{
					sendMyNewIpToNoIpServer(savedIp);
					writeMyNewIpToCfgFile();
				}

				if (isError)
				{
					isError = false;
					howException = 0;
				}
			} catch (Exception e)
			{
				howException++;
				if (!isError)
				{
					log.println("******** Wystapil b³¹d!!!!!!" + e.getMessage());
					log.println("Zdarzenie z godziny: " + DateTime.now().toString("yyyy-MM-dd HH:mm"));
					e.printStackTrace(log);
				}
				isError = true;

				//				log.println(DateTime.now().toString("yyyy-MM-dd HH:mm") + " wystapil wyjatek " + e.getMessage());
			}
		}

		log.println(DateTime.now().toString("yyyy-MM-dd HH:mm") + ": Koniec procesu...");
	}

	private static String readIpFromCfgFile()
	{
		NoIpConfigFile cfgFile = new NoIpConfigFile();
		String savedIp = cfgFile.readIpFromFile();
		if (savedIp == null || savedIp.isEmpty())
		{
			cfgFile.writeIpToFile(myIp);
		}
		return savedIp;
	}

	private static void writeMyNewIpToCfgFile()
	{
		NoIpConfigFile cfgFile = new NoIpConfigFile();
		cfgFile.writeIpToFile(myIp);
		cfgFile = null;
		log.println(DateTime.now().toString("yyyy-MM-dd HH:mm") + ": Zapisalem nowe IP do pliku.");
	}

	private static void sendMyNewIpToNoIpServer(String savedIp) throws MalformedURLException, IOException,
			ClientProtocolException
	{
		log.println(DateTime.now().toString("yyyy-MM-dd HH:mm") + ": Stare ip to: " + savedIp + " a nowe to: " + myIp
				+ " wiec chce zaktualizowac w NOIP");
		URL url = new URL("http://radmit:morawski87@dynupdate.no-ip.com/nic/update?hostname=radmit.noip.me&myip="
				+ myIp);

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url.toString());

		// add request header
		request.addHeader("User-Agent", "RADMIT Update Client v0.1 radekm87@gmail.com");

		HttpResponse response = client.execute(request);

		int resultCode = response.getStatusLine().getStatusCode();

		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println(DateTime.now().toString("yyyy-MM-dd HH:mm") + ": Response Code : " + resultCode);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null)
		{
			result.append(line);
		}

		request = null;
		client = null;
		log.println(result.toString());

		if (resultCode != 200 || !(result.toString().contains("nochg") || result.toString().contains("good")))
		{
			throw new ClientProtocolException("Otrzymano status b³edu z noip.me. Nie aktualizuje informacji w pliku.");
		}
	}

	private static void getMyIpFromAmazon() throws MalformedURLException, IOException
	{
		URL urlIp = new URL("http://checkip.amazonaws.com/");
		BufferedReader br = new BufferedReader(new InputStreamReader(urlIp.openStream()));
		myIp = br.readLine();
	}

	private static void getMyIpFromAnotherWWW() throws MalformedURLException, IOException
	{
		URL urlIp = new URL("http://www.trackip.net/ip");
		BufferedReader br = new BufferedReader(new InputStreamReader(urlIp.openStream()));
		myIp = br.readLine();
	}
}
