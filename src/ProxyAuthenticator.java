import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Created by stuartkuredjian on 6/1/15.
 */
public class ProxyAuthenticator extends Authenticator{
    private String proxyUsername;
    private String proxyPassword;

    public ProxyAuthenticator(String proxyUsername, String proxyPassword) {
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.proxyUsername, proxyPassword.toCharArray());
    }
}
