import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by stuartkuredjian on 5/30/15.
 */
public class URLVisitor {
    private static boolean shouldPrintStatus = true;
    private String url = "";
    private URL URL;
    private HttpURLConnection conn;
    private String params;
    private String method = "GET";
    private String response;
    private String sessionCookie = "";
    private ConnectionManager connMgr;
    private Utilities utils = new Utilities();
    private int responseCode;
    private Boolean shouldOutput = true;
    private boolean isWatching = false;
    private boolean singleCheck = false;
    private boolean isConnected = false;
    private String protocol = "";
    private AccountManager accountMgr;
    private boolean authCodeIsSet = false;
    private ArrayList outputArray = new ArrayList();
    private boolean shouldPrint = true;

    public URLVisitor(AccountManager accountMgr) {
        this.accountMgr = accountMgr;
        if(this.sessionCookie != "") {
            conn.setRequestProperty("Cookie", this.sessionCookie);
        }
    }

    public void setURL(String url) {
        this.url = url;
        try {
            authCodeIsSet = accountMgr.getAuthCodeIsSet();
            if (authCodeIsSet) {
                protocol = accountMgr.getProtocol();
                String urlProtocol = url.substring(0, url.indexOf(":"));
                if (!urlProtocol.equals(protocol)) {
                    url = url.replace(urlProtocol, protocol);
                }
            }
            this.URL = new URL(url);
        } catch (MalformedURLException e) {
            utils.println("MalformedURLException");
            e.printStackTrace();
        }
    }

    public void setShouldOutput(Boolean shouldOutput) {
        this.shouldOutput = shouldOutput;
    }

    public void execute() {
        int connectAttempts = 0;
        isConnected = false;
        while(!isConnected) {
            connectAttempts++;
            if(connectAttempts == 9) {
                isConnected = false;
                break;
            }
            connMgr = new ConnectionManager(this.URL, this.method, this.params);
            if (!this.sessionCookie.equals("")) {
                connMgr.useCookie(this.sessionCookie);
            }
            connMgr.setShouldPrint(this.shouldPrint);
            connMgr.setShouldOutput(this.shouldOutput);
            connMgr.setIsWatching(isWatching);
            connMgr.setSingleCheck(singleCheck);

            connMgr.connect(this.params);
            this.responseCode = connMgr.getResponseCode();
            isConnected = connMgr.getIsConnected();
            if(responseCode==200) {
                authCodeIsSet = accountMgr.getAuthCodeIsSet();
                if(!authCodeIsSet) {
                    protocol = connMgr.getProtocol();
                }
            }
            if (isConnected) {
                this.sessionCookie = connMgr.getSessionCookie();
                this.response = connMgr.getResponse();
            } else {
                if(responseCode == 404) {
                    break;
                }
            }
        }
    }

    public String getProtocol() {
        return this.protocol;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public String getSessionCookie() {
        return this.sessionCookie;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResponse() {
        return response;
    }

    public java.net.URL getURL() {
        return URL;
    }

    public void setSessionCookie(String requestCookie) {
        this.sessionCookie = requestCookie;
    }

    public void setIsWatching(boolean isWatching) {
        this.isWatching = isWatching;
    }

    public void setSingleCheck(boolean singleCheck) {
        this.singleCheck = singleCheck;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public void setShouldPrint(boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }

    public static boolean getShouldPrintStatus() {
        return shouldPrintStatus;
    }

    public void setShouldPrintStatus(boolean b) {
        this.shouldPrintStatus = b;
    }
}
