package pl.radmit.noIpService.models;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Created by Radek on 23.08.2016.
 * Odczytuje konfigurację do konta noip z pliku ./noIpConfig.ini
 */
public class NoIpConfiguration {
    private String username = null;
    private String password = null;
    private String hostname = null;

    public NoIpConfiguration() {
        initConfigurationFromIniFile();
    }

    private void initConfigurationFromIniFile() {
        Path path = FileSystems.getDefault().getPath("noIpConfig.ini");
        Wini ini = null;
        try {
            ini = new Wini(path.toFile());
        } catch (IOException e) {
            System.out.println("Brak pliku konfiguracyjnego noIpConfig.ini");
            e.printStackTrace();
        }

        this.username = ini.get("account", "username");
        this.password = ini.get("account", "password");
        this.hostname = ini.get("account", "hostname");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isValid() {
        if(username == null || password == null || hostname == null
            || username.isEmpty() || password.isEmpty() || hostname.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
