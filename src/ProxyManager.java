import java.net.Authenticator;
import java.util.Properties;

/**
 * Created by stuartkuredjian on 6/1/15.
 */
public class ProxyManager {
    private String address;
    private String port;
    private String username;
    private String password;
    private Boolean shouldAuthenticate = false;


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setProxy(Boolean useProxy) {
        if(useProxy) {
            System.setProperty("http.proxyHost", this.address);
            System.setProperty("http.proxyPort", this.port);
            System.setProperty("java.net.useSystemProxies", "true");

            if(this.shouldAuthenticate) {
                System.setProperty("http.proxyUser", this.username);
                System.setProperty("http.proxyPassword", this.password);
            }

            System.setProperty("http.proxySet", "true");
        } else {
            Properties prop = System.getProperties();
            prop.remove("http.proxyHost");
            prop.remove("http.proxyPort");
            prop.remove("http.proxyUser");
            prop.remove("http.proxyPassword");
            prop.remove("http.proxySet");
            Authenticator.setDefault(null);
        }
    }

    public void setShouldAuthenticate(Boolean shouldAuthenticate) {
        this.shouldAuthenticate = shouldAuthenticate;
    }
}

