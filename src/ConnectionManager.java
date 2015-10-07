import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by stuartkuredjian on 5/30/15.
 */
public class ConnectionManager {
    private boolean shouldPrintStatus = true;
    private boolean methodSucceeded = false;
    private URL URL;
    public HttpURLConnection conn;
    private String sessionCookie = "";
    private String response = "";
    private String params;
    private String method = "GET";
    private Proxy proxy = null;
    private int responseCode;
    private Boolean shouldOutput = true;
    private Utilities utils = new Utilities();
    private boolean isWatching;
    private boolean singleCheck = false;
    private boolean isConnected = false;
    private String protocol = "";
    private boolean shouldPrint = true;
    private ArrayList outputArray;


    public ConnectionManager(URL URL, String method, String params) {
        shouldPrintStatus = URLVisitor.getShouldPrintStatus();
        outputArray = OkcApp.getOutputArray();
        this.URL = URL;
        this.method = method;
        this.params = params;
        while(!methodSucceeded) {
            onLoad(URL, method);
        }
    }

    private void onLoad(URL URL, String method) {
        try {
//            utils.println("http.proxySet: " + System.getProperty("http.proxySet"));
            if(System.getProperty("http.proxySet") == "true") {
                this.proxy = new Proxy(
                        Proxy.Type.HTTP,
                        new InetSocketAddress(
                                System.getProperty("http.proxyHost"),
                                Integer.parseInt(System.getProperty("http.proxyPort"))
                        )
                );
                Authenticator.setDefault(
                        new ProxyAuthenticator(
                                System.getProperty("http.proxyUser"),
                                System.getProperty("http.proxyPassword")
                        )
                );
            }

            if(proxy !=null) {
                conn = (HttpURLConnection) URL.openConnection(proxy);
                responseCode = conn.getResponseCode();
                conn.disconnect();
                conn = (HttpURLConnection) URL.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) URL.openConnection();
                responseCode = conn.getResponseCode();
                conn.disconnect();
                conn = (HttpURLConnection) URL.openConnection();
            }

            conn.setRequestMethod(method);
            methodSucceeded = true;

        } catch (IOException e) {
            utils.println("\nIOException");
            e.printStackTrace();
            utils.println("Retrying connection...", false);
        }
    }

    public void connect(String params) {
        try {
            utils.setShouldPrint(this.shouldPrint);
            methodSucceeded = false;
            while(methodSucceeded == false) {
                checkMethod(this.method);
            }
            responseCode = conn.getResponseCode();
            if(!isWatching || singleCheck) {
                if(shouldPrint) {
                    if(shouldPrintStatus) {
                        utils.print(" " + responseCode, false, false);
                    }
                }
            }
            if(responseCode == 404) {
                System.out.println(": Page not found");
                outputArray.add(": Page not found\n");
            } else {
                while (responseCode != 200) {
                    if(responseCode == 404) {
                        break;
                    }
                    utils.print(": Redirecting\n", false, false);

                    URL = new URL(conn.getHeaderField("Location"));
                    utils.print(String.valueOf(URL), false);
                    if(proxy !=null) {
                        conn = (HttpURLConnection) URL.openConnection(proxy);
                    } else {
                        conn = (HttpURLConnection) URL.openConnection();
                    }
                    useCookie(sessionCookie);
                    methodSucceeded = false;
                    while(methodSucceeded == false) {
                        checkMethod(this.method);
                    }

                    responseCode = conn.getResponseCode();

                    if (!isWatching && !singleCheck) {
                        if(shouldPrintStatus) {
                            utils.print(" " + responseCode, false, false);
                        }
                    }
                }

                if (responseCode == 200) {
                    String url = String.valueOf(URL);
                    protocol = url.substring(0, url.indexOf(":"));
                    if(!isWatching) {
                        if(shouldPrint) {
                            if(shouldPrintStatus) {
                                utils.println(": OK", false, false);
                            }
                        }
                    }
                        InputStream is = conn.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        String line = null;
                        StringBuffer response = new StringBuffer();
                        while ((line = rd.readLine()) != null) {
                            response.append(line);
                            response.append('\n');
                        }
                        rd.close();
                        this.response = response.toString();
//            utils.println("Response: "+ this.response);

                    if (!isWatching || singleCheck) {
                        String[] Keys = new String[100];
                        String[] Values = new String[100];
                        for (int i = 0; i <= conn.getHeaderFields().size(); i++) {
                            Keys[i] = conn.getHeaderFieldKey(i);
                            Values[i] = conn.getHeaderField(i);
                            if (Keys[i] == null) {
                                Keys[i] = "";
                            }
                            if (Keys[i].compareTo("Set-Cookie") == 0) {
                                if (Values[i].startsWith("session=")) {
                                    this.sessionCookie = this.sessionCookie + Values[i] + "   ;   ";
                                }
                            }
                        }
                    }
                    isConnected = true;
                }
                setResponseCode(responseCode);
            }
        } catch (IOException e) {
            utils.println("\nIOException");
            if(!isWatching) {
                e.printStackTrace();
            }
            utils.println("Retrying connect(" + params + ")...", false);
        }
    }

    public String getProtocol() {
        return this.protocol;
    }

    public URL getURL() {
        return this.URL;
    }

    private void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    private void checkMethod(String method) {
        methodSucceeded = false;
        try {
            if (method.equals("POST")) {
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();
            } else {
                shouldOutput = false;
                conn.setDoOutput(false);
            }
            methodSucceeded = true;
        }catch (IOException e) {
            utils.println("\nIOException");
            e.printStackTrace();
            methodSucceeded = false;
        }
    }

    public String getResponse() {
        return this.response;
    }

    public void useCookie(String sessionCookie) {
        setSessionCookie(sessionCookie);
        conn.setRequestProperty("Cookie", sessionCookie);
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setShouldOutput(Boolean shouldOutput) {
        this.shouldOutput = shouldOutput;
    }

    public void setIsWatching(boolean isWatching) {
        this.isWatching = isWatching;
    }

    public void setSingleCheck(boolean singleCheck) {
        this.singleCheck = singleCheck;
    }

    public boolean getMethodSucceeded() {
        return methodSucceeded;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public void setShouldPrint(boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }

}