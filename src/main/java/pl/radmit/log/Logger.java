package pl.radmit.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * Created by Radek on 21.08.2016.
 */
public class Logger {

    private PrintStream log;
    private ILoggerConfigurator configurator;
    private static final String dateTimePatternFormat = "yyyy-MM-dd HH:mm";

    public Logger(ILoggerConfigurator configurator) {
        this.configurator = configurator;
        this.log = prepareAndRunLogger();
    }

    private PrintStream prepareAndRunLogger()
    {
        try
        {
            PrintStream s = new PrintStream(configurator.getLogPath());
            System.setErr(s);
            System.setOut(s);
            return s;
        } catch (FileNotFoundException e)
        {
            System.out.println("Błąd podczas tworzenia pliku logu.");
            e.printStackTrace();
        }
        return null;
    }

    public void log(String info) {
        log.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateTimePatternFormat)) + ": " + info);
    }

    public PrintStream getPrintStream() {
        return log;
    }
}
