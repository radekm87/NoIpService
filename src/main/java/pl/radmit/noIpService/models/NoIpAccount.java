package pl.radmit.noIpService.models;

/**
 * Created by Radek on 23.08.2016.
 */
public class NoIpAccount {
    private String username;
    private String password;
    private String hostname;

    public NoIpAccount(String username, String password, String hostname) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
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
}
