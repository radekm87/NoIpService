package pl.radmit.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Radek on 21.08.2016.
 */
public class NoIpLoggerConfiguratorImpl implements ILoggerConfigurator {
    @Override
    public File getLogPath() {
        return new File("NoIpLogFile"
                + new SimpleDateFormat("yyyyMMdd_HHmm").format(Calendar.getInstance().getTime()));
    }
}
