import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class NoIpConfigFile
{
	public String readIpFromFile()
	{
		Charset charset = Charset.forName("CP1250");
		Path path = FileSystems.getDefault().getPath("noIpConfig");
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(path, charset))
		{
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}
		} catch (IOException x)
		{
			System.err.format("IOException: %s%n", x);
		}

		return sb.toString();
	}

	public void writeIpToFile(String newIp)
	{
		Charset charset = Charset.forName("CP1250");
		Path path = FileSystems.getDefault().getPath("noIpConfig");
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset))
		{
			writer.write(newIp, 0, newIp.length());
		} catch (IOException x)
		{
			System.err.format("IOException: %s%n", x);
		}

	}
}
