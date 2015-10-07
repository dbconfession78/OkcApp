import javax.swing.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stuartkuredjian on 5/19/15.
 */
public class AccountManager {
    private Preferences _prefs;
    public MainView mainView;
    private SearchPreferencesView searchPreferencesView;
    private ManageAccountsView manageAccountsView;
    private ProxyManager proxyManager;
    public int index;
    public URLVisitor vis;
    public HashMap<String, String> loginSettings;
    private Utilities utils;
    private String username;
    private String account;
    private int accountCount = 0;
    private Iterator iterator;
    private String authCode;
    private boolean isLoggedIn = false;
    private Pattern pattern;
    private Matcher matcher;
    private HashMap<String, String> searchSettingsMap;
    private String response;
    private String locId;
    private ArrayList<String> usernames;
    private String sessionCookie;
    private ArrayList<String> watchedProfiles;
    private ArrayList<String> bannedProfiles = new ArrayList<>();
    private String bannedTime = "";
    private boolean isRefreshing = false;
    private String bannedDate = "";
    private String protocol = "";
    private Boolean authCodeIsSet = false;
    private boolean profileExists = false;
    private boolean isUsingAlt = false;
    private ArrayList outputArray;
    private Thread loginThread;
    private Boolean lastRunCompleted = true;
    private String endTime;
    private HashMap runState = new HashMap();
    private  ArrayList<String> closedProfiles = new ArrayList<>();
    private Thread deleteInboxThread;

    public AccountManager(MainView mainView) {
        outputArray = OkcApp.getOutputArray();
        this.mainView = mainView;
        utils = new Utilities();
        this.manageAccountsView = new ManageAccountsView(this);
        this.searchPreferencesView = new SearchPreferencesView(this);
        this.index = fetchIndex();
        this.accountCount = fetchAccountCount();
    }

    public Boolean getAuthCodeIsSet() {
        return authCodeIsSet;
    }
    
    public void backupLogin(String profile, String password, String zip, String proxy, String port) {
        utils.println("\nAlternative login with:", false);
        utils.println("username: " + profile, false);
        utils.println("password: " + password, false);
        if(proxy.equals("")) {
            utils.println("proxy: none\n", false);
        } else {
            utils.println("proxy: " + proxy + ":" + port + "\n", false);
        }

        utils.println("Getting location ID...", false);
        vis = new URLVisitor(this);
        vis.setURL("https://www.okcupid.com/locquery?func=query&query=" + zip);
        vis.setMethod("GET");
        utils.println(String.valueOf(vis.getURL()), false);
        vis.execute();
        this.response = vis.getResponse();
//        utils.println(this.response);

        pattern = Pattern.compile("\"locid\"\\s:\\s([0-9]+)");
        matcher = pattern.matcher(this.response);
        matcher.find();
        this.locId = matcher.group(1);
        utils.println("locId:  " + locId, false);

        utils.println("\nGetting session cookie...", false);
        vis = new URLVisitor(this);
        vis.setURL("https://www.okcupid.com/login");
        vis.setParams("username=" + profile + "&password=" + password + "&okc_api=1");
        vis.setMethod("POST");

        utils.print(String.valueOf(vis.getURL()), false);
        vis.execute();
        this.sessionCookie = vis.getSessionCookie();

        utils.println("session=" + utils.findText(sessionCookie, "session=", ";") + "\n", false);
        this.response = vis.getResponse();
//        utils.println(this.response);

        utils.println("Getting Authcode from Homepage...", false);
        vis = new URLVisitor(this);
        vis.setURL("http://www.okcupid.com/home");
        vis.setMethod("GET");
        vis.setParams("");

        utils.print(String.valueOf(vis.getURL()), false);
        vis.setSessionCookie(this.sessionCookie);
        vis.execute();
        this.response = vis.getResponse();

        extractAuthCode();
        protocol = vis.getProtocol();
//        utils.println(this.response);

        pattern = Pattern.compile("body id=\"p_home\"");
        matcher = pattern.matcher(response);
        matcher.find();
        try {
            isLoggedIn = true;
        } catch (Exception e) {
            utils.println("Unable to retrieve home page", true);
            utils.killAllThreadOccurances(loginThread);
        }

        mainView.toggleRestrictedComponents();

    }

    public void login(final String aUsername) {
        loginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Login initializations

                Thread.currentThread().setName("Login Thread");
                username = aUsername;
                account = getAccountFromUsername(aUsername);
                changeNode(account);
                loginSettings = fetchLoginSettings(aUsername);
                searchSettingsMap =  fetchSearchSettings(aUsername);
                proxyManager = new ProxyManager();
                setProxySettings();
                String password = String.valueOf(loginSettings.get("password"));
                String zip = String.valueOf(loginSettings.get("zip"));
                String proxy = String.valueOf(loginSettings.get("proxy"));
                String port = String.valueOf(loginSettings.get("port"));

                // check if proxy is being used
                if(!proxy.equals("")) {
                    proxyManager.setProxy(true);
                } else {
                    proxyManager.setProxy(false);
                }

                // check if is using alternate account for login
                if(isUsingAlt) {
                    // alternative login
                    utils.println("Watcher login with:", false);
                    utils.println("username: " + aUsername, false);
                    utils.println("password: " + password, false);
                    if(proxy.equals("")) {
                        utils.println("proxy: none\n", false);
                    } else {
                        utils.println("proxy: " + proxy + ":" + port + "\n", false);
                    }
                    utils.println("Getting location ID...", false);
                } else {
                    // regular login
                    utils.println("\nLogging in with:");
                    utils.println("username: " + aUsername);
                    utils.println("password: " + password);
                    if(proxy.equals("")) {
                        utils.println("proxy: none\n");
                    } else {
                        utils.println("proxy: " + proxy + ":" + port + "\n");
                    }

                    utils.println("Getting location ID...");
                }

                // get location ID
                vis = new URLVisitor(getAccountManager());
                vis.setURL("https://www.okcupid.com/locquery?func=query&query=" + zip);
                vis.setParams("");
                vis.setMethod("GET");
                if(isUsingAlt) {
                    utils.print(String.valueOf(vis.getURL()), false);
                } else {
                    utils.print(String.valueOf(vis.getURL()));
                }
                vis.execute();
                response = vis.getResponse();
                locId = utils.findString(response, "\"locid\"\\s:\\s([0-9]+)", 1);

                if(isUsingAlt) {
                    utils.println("locId:  " + locId, false);
                    utils.println("\nGetting session cookie...", false);
                } else {
                    utils.println("locId:  " + locId);
                    utils.println("\nGetting session cookie...");
                }

                // get session cookie
                vis = new URLVisitor(getAccountManager());
                vis.setURL("https://www.okcupid.com/login");
                vis.setParams("username=" + aUsername + "&password=" + password + "&okc_api=1");
                vis.setMethod("POST");
                if(isUsingAlt) {
                    utils.print(String.valueOf(vis.getURL()), false);
                } else {
                    utils.print(String.valueOf(vis.getURL()));
                }

                vis.execute();
                sessionCookie = vis.getSessionCookie();
                if(isUsingAlt) {
                    utils.println("session=" + utils.findText(sessionCookie, "session=", ";") + "\n", false);
                } else {
                    utils.println("session=" + utils.findText(sessionCookie, "session=", ";") + "\n");
                }
                response = vis.getResponse();
//        utils.println(this.response);

                if(isUsingAlt) {
                    utils.println("Getting Authcode from Homepage...", false);
                } else {
                    utils.println("Getting Authcode from Homepage...");
                }

                // get homepage
                vis = new URLVisitor(getAccountManager());
                vis.setURL("http://www.okcupid.com/home");
                vis.setMethod("GET");
                vis.setParams("");
                if(isUsingAlt) {
                    utils.print(String.valueOf(vis.getURL()), false);
                } else {
                    utils.print(String.valueOf(vis.getURL()));
                }
                vis.setSessionCookie(sessionCookie);
                vis.execute();


                response = vis.getResponse();
                protocol = vis.getProtocol();
                String targetString = utils.findString(response, "body id=\"p_home\"", 0);
                if(!targetString.isEmpty()) {
                    isLoggedIn = true;
                } else {
                    utils.println("Login failure: unable to retrieve home page");
                    isLoggedIn = false;
                    utils.killAllThreadOccurances(loginThread);
                }
                if(isLoggedIn) {
                    // get authcode
                    extractAuthCode();
                    mainView.toggleRestrictedComponents();
                    setUiToLastRunState();
                }
            }
        });
        loginThread.start();
    }



    private void setUiToLastRunState() {
        runState = fetchRunState();

        endTime = String.valueOf(runState.get("endTime"));
        if(endTime.equals("RUNNING")) {
            String visitsCompleted = String.valueOf(runState.get("visitsCompleted"));
            String visitLimit = String.valueOf(runState.get("visitLimit"));
            String startTime = String.valueOf(runState.get("startTime"));
            String elapsed = String.valueOf(runState.get("elapsed"));
            String totalVisits = String.valueOf(runState.get("totalVisits"));
            String completedRuns = String.valueOf(runState.get("completedRuns"));

            lastRunCompleted = false;
            mainView.setVisitsCompleted(Integer.parseInt(visitsCompleted));
            mainView.setVisitLimitUI("of " + visitLimit);
            mainView.setStartTime(startTime);
            mainView.setRunElapsedUI(elapsed);
            mainView.setTotalVisitsUI(totalVisits);
        } else {
            lastRunCompleted = true;
            String limit = String.valueOf(searchSettingsMap.get("limit"));
            if(!limit.equals("") && !limit.equals(("0"))) {
                mainView.runButton.setEnabled(true);
            } else {
                mainView.runButton.setEnabled(false);
            }
            runState.put("visitsCompleted", "0");
        }
    }

    public HashMap fetchRunState() {
        HashMap runState = new HashMap();
        try {
            changeNode(account + "/runState");
            String[] keys = _prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                String value = _prefs.get(key, "");
                runState.put(key, value);
            }
            for (int i = 0; i < runState.size(); i++) {
                if(runState.containsValue("")) {
                    System.out.println("");
                }
            }
        } catch (BackingStoreException e) {
            utils.print("Failed to fetch run state", true);
            e.printStackTrace();
        }

        return runState;
    }

    public ArrayList fetchNewVisitors() {
        ArrayList newVisitors = new ArrayList();

        URLVisitor vis = new URLVisitor(this);
        vis.setMethod("GET");
        vis.setSessionCookie(this.sessionCookie);
        vis.setURL(protocol + "://www.okcupid.com/visitors");
        vis.setShouldPrint(false);
        vis.execute();
        vis.setShouldPrint(true);
        this.response = vis.getResponse();
//        System.out.println(this.response);

        Pattern p = Pattern.compile("class=\"name\"\\s>([\\w\\\\ÆÐƎƏƐƔĲŊŒ\u1E9EÞǷȜæðǝəɛɣĳŋœĸſßþƿȝĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţțŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗðéèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîïǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœŔŘŖŚŜŠŞȘṢ\u1E9EŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃẁŵẅƿýỳŷÿȳỹƴźżžẓ±-]+)</a>");
        Matcher m = p.matcher(this.response);
        while(m.find()) {
            String visitor = m.group(1);
            newVisitors.add(visitor);
        }

        return newVisitors;
    }

    public String fetchNewVisitorCount() {
        String count = "0";
        URLVisitor vis = new URLVisitor(this);
        vis.setMethod("GET");
        vis.setSessionCookie(this.sessionCookie);
        vis.setURL(protocol + "://www.okcupid.com/home");
        vis.setShouldPrint(false);
        vis.execute();
        vis.setShouldPrint(true);
        this.response = vis.getResponse();

        count = utils.findString(response, "<span id=\"nav_visitors_badge\" class=\"badge\"> <span class=\"count\"> ([0-9]+)", 1);
        if(count == null) {
            count = "0";
        }
        return count;
    }

    private void extractAuthCode() {
        this.authCode = utils.findString(response, "authcode\"\\svalue=\"([0-9a-z,;%]+)\"", 1);
        if(authCode != null) {
            utils.println("AUTHCODE: " + this.authCode);
            authCodeIsSet = true;
            if (isUsingAlt) {
                utils.println("\nLogged In\n", false);
            } else {
                utils.println("\nLogged In\n", true);
            }
            mainView.addAccountButton.firePropertyChange("visible", true, false);
        } else {
            isLoggedIn = false;
            utils.println("Unable to retrieve AuthCode", true);
            utils.killAllThreadOccurances(loginThread);
        }
    }

    public String getSessionCookie() {
        return this.sessionCookie;
    }

    public HashMap<String, String> fetchSearchSettings(String username) {
        try {
            searchSettingsMap = new HashMap<String, String>();
            changeNode(getAccountFromUsername(username));
            String[] keys = _prefs.keys();


            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if(key.equals("orderBy")) {
                    searchSettingsMap.put(key, _prefs.get(key, ""));
                } else {
                    searchSettingsMap.put(key, _prefs.get(key, "").toLowerCase());
                }

            }

            for (int i = 0; i < searchSettingsMap.size(); i++) {
                if(loginSettings.containsKey(keys[i])) {
                    searchSettingsMap.remove("username");
                    searchSettingsMap.remove(keys[i]);
                }
            }

        } catch (BackingStoreException e) {
            utils.print("Failed to fetch Search Settings", true);
            utils.println(":  BackingStoreException");
            e.printStackTrace();
        }
        return searchSettingsMap;
    }

    public HashMap<String, String> fetchLoginSettings(String username) {
        loginSettings = new HashMap();
        changeNode(getAccountFromUsername(username));
        String[] keys = new String[] {"password", "proxy", "port", "zip", "proxyUsername", "proxyPassword"};
        for (int i = 0; i < keys.length; i++) {
            loginSettings.put(keys[i], _prefs.get(keys[i], ""));
        }

        return loginSettings;
    }

    public String getAccountFromUsername(String username) {
        prefsRoot();
        try {
            String[] accounts = _prefs.childrenNames();
            for (int i = 0; i < accounts.length; i++) {
                changeNode(accounts[i]);
                if(_prefs.get("username", "").equals(username)) {
                    return accounts[i];
                }
            }
        } catch (BackingStoreException e) {
            utils.print("Failed to get account", true);
            utils.println(":  BackingStoreException");
            e.printStackTrace();
        }
        utils.println("Login information for " + username + " has not been entered yet.");
        return null;
    }

    private void setProxySettings() {
        if(!loginSettings.get("proxy").equals("")) {
            proxyManager.setAddress(loginSettings.get("proxy"));
            proxyManager.setPort(loginSettings.get("port"));
            proxyManager.setUsername(loginSettings.get("proxyUsername"));
            proxyManager.setPassword(loginSettings.get("proxyPassword"));

            if(!loginSettings.get("proxy").equals("")) {
                proxyManager.setShouldAuthenticate(true);
            } else {
                proxyManager.setShouldAuthenticate(false);
            }
        }
    }

    private int fetchIndex() {
        prefsRoot();
        int index = 0;
        if(_prefs.get("index", "").equals("")) {
            _prefs.put("index", String.valueOf(index));
            return index;
        }
        index = Integer.parseInt(_prefs.get("index", ""));
        return index;
    }

    public ManageAccountsView getManageAccountsView() {
        return this.manageAccountsView;
    }

    public void deleteAccount(JComboBox jComboBox) {
        try {
            int oldCount = fetchAccountCount();
            String username = String.valueOf(jComboBox.getSelectedItem());
            for (int i = 0; i < index; i++) {
                prefsRoot();
                String account = "account-" + i;
                if (_prefs.nodeExists(account)) {
                    changeNode(account);
                    if(_prefs.get("username", "").equals(username)) {
                        _prefs.removeNode();
                        int newCount = fetchAccountCount();
                        jComboBox.firePropertyChange("itemCount", oldCount, newCount);
                        prefsRoot();
                        break;
                    }
                }
            }
        } catch (BackingStoreException e) {
            utils.print("Account delete failed", true);
            utils.println(":  BackingStoreException");
            e.printStackTrace();
        }
    }

    public void setUsernameComponent(JComboBox jComboBox, JButton jButton) {
        boolean oldVisible = mainView.addAccountButton.isVisible();
        if(fetchAccountCount() > 0) {
            jComboBox.setVisible(true);
            jButton.setVisible(false);
            mainView.loginButton.setEnabled(true);
        } else {
            jComboBox.setVisible(false);
            jButton.setVisible(true);
            mainView.loginButton.setEnabled(false);
        }
        boolean newVisible = mainView.addAccountButton.isVisible();
        mainView.addAccountButton.firePropertyChange("visible", oldVisible, newVisible);
    }

    public void populateUsernames(JComboBox jComboBox) {
        String oldSelectedUsername= String.valueOf(jComboBox.getSelectedItem());
        jComboBox.removeAllItems();
        accountCount = fetchAccountCount();
        usernames = fetchUsernames();
        for (int i = 0; i < usernames.size(); i++) {
            jComboBox.addItem(usernames.get(i));
        }

        for (int i = 0; i < jComboBox.getItemCount(); i++) {
            if(jComboBox.getItemAt(i).equals(oldSelectedUsername));
            jComboBox.setSelectedItem(oldSelectedUsername);
        }
        if(manageAccountsView != null && manageAccountsView.isVisible()) {
            setUsernameComponent(jComboBox, manageAccountsView.addAccountButton);
        }
        setUsernameComponent(jComboBox, mainView.addAccountButton);
    }

    public ArrayList<String> fetchUsernames() {
        prefsRoot();
        usernames = new ArrayList();
        for (int i = 0; i < this.index; i++) {
            try {
                prefsRoot();
                if(_prefs.childrenNames().length == 0) {
                    break;
                }
                if (_prefs.nodeExists("account-" + i)) {
                    changeNode("account-" + i);
                    usernames.add(_prefs.get("username", ""));
                }
            } catch (BackingStoreException e) {
                utils.print("Failed to fetch usernames", true);
                utils.println(":  BackingStoreException");
                e.printStackTrace();
            }
        }
        return usernames;
    }

    public void changeNode(String node) {
        if(node == null) {
            utils.println("No such node");
        } else {
            this._prefs = Preferences.userRoot().node("OkcAccounts/" + node);
        }
    }

    public void showAddAccountView() {
        new AddAccountView(this).setVisible(true);
    }

    public int fetchAccountCount() {
        changeNode("bannedProfiles");
        prefsRoot();
        try {
            int childCount = _prefs.childrenNames().length;
            int accountCount = 0;

            String[] childrenNames = _prefs.childrenNames();
            for (int i = 0; i < childCount; i++) {
                if (childrenNames[i].startsWith("account")) {
                    accountCount++;
                }
            }
            return accountCount;
        } catch (BackingStoreException e) {
            utils.print("Failed to fetch number of accounts", true);
            e.printStackTrace();
        }
        return 0;
    }

    public void prefsRoot() {
        _prefs = Preferences.userRoot().node("OkcAccounts");
    }

    public void addAccount(HashMap<String, String> loginSettingsMap, HashMap<String, String> searchSettingsMap) {
        SearchPreferencesView searchPreferencesView = new SearchPreferencesView(this);
        searchSettingsMap = searchPreferencesView.getSearchSettingsMap();
        utils.populateMap(searchPreferencesView.panel1, searchSettingsMap);
        changeNode("account-" + (index));

        publishMap(loginSettingsMap);
        publishMap(searchSettingsMap);

        accountCount = fetchAccountCount();
        incrementIndex(index);
    }

    public void publishMap(HashMap<String, String> userInputMap) {
        iterator = userInputMap.keySet().iterator();
        while(iterator.hasNext()) {
            String next = String.valueOf(iterator.next());
            _prefs.put(next, userInputMap.get(next));
        }
    }

    private void incrementIndex(int index) {
        prefsRoot();
        _prefs.put("index", String.valueOf(index + 1));
        this.index = fetchIndex();
    }

    public void logout() {
        isLoggedIn = false;
        if(!isRefreshing) {
            utils.killAllThreads(isLoggedIn);
            mainView.toggleRestrictedComponents();
        } else {
            utils.stopThread("Visitor Count Thread");
        }
        utils.println("Logged Out", true);
        isRefreshing = false;
    }

    public void clearAccounts() {
        prefsRoot();
        try {
            String[] childrenNames = _prefs.childrenNames();

            for (int i = 0; i < childrenNames.length-1; i++) {
                String childName = childrenNames[i];
                if(childName.startsWith("account")) {
                    changeNode(childName);
                    _prefs.removeNode();
                }
            }
        } catch (BackingStoreException e) {
            utils.print("Failed to clear accounts", true);
            utils.println(":  BackingStoreException");
            e.printStackTrace();
        }
    }

    public void resetIndex() {
        prefsRoot();
        _prefs.put("index", "1");
    }

    public void updateUserPreferences(HashMap<String, String> settingsMap) {
        String username = "";
        if(isLoggedIn) {
            username = getUsername();
        } else {
            username = settingsMap.get("username");
        }
        String account = getAccountFromUsername(username);
        changeNode(account);

        publishMap(settingsMap);
        this.searchSettingsMap = fetchSearchSettings(username);
    }



    public String getUsername() {
        String username = String.valueOf(mainView.usernamesCombo.getSelectedItem());
        return username;
    }

    public ArrayList<String> fetchWatchers() {
        try {
            watchedProfiles = new ArrayList<>();
            changeNode(account + "/watchers");
            String[] watcherKeys = _prefs.keys();

            for (int i = 0; i < watcherKeys.length; i++) {
                String watcherKey = watcherKeys[i];
                watchedProfiles.add(_prefs.get(watcherKey, ""));
            }
        } catch (BackingStoreException e) {
            utils.print("Failed to fetch watchers", true);
            utils.println(":  BackingStoreException");
            e.printStackTrace();
        }
        return watchedProfiles;
    }

    public HashMap<String, String> getSearchSettingsMap() {
        searchSettingsMap = fetchSearchSettings(getUsername());
        return searchSettingsMap;
    }

    public AccountManager getAccountManager() {
        return this;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public void deleteInbox() {
        deleteInboxThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                mainView.runButton.setEnabled(false);
                utils.println("Deleting Inbox...", true);
                int messageCount = getMessageCount();
                if(messageCount == 0) {
                    utils.println("The inbox was already empty.", true);
                } else {
                    int initialMessageCount = messageCount;
                    while (messageCount > 0) {
                        ArrayList messageIds = getMessageIds();
                        String url = "https://www.okcupid.com/apitun/messages/threads?&access_token=" + authCode + "&access_token=" + authCode + "&threadids=[";
                        for (int i = 0; i < messageIds.size(); i++) {
                            url += "\"" + messageIds.get(i) + "\"";
                            if (i != (messageIds.size() - 1)) {
                                url += ",";
                            } else {
                                url += "]&_method=DELETE";
                            }
                        }
                        vis.setURL(url);
                        vis.setMethod("GET");
                        vis.setSessionCookie(sessionCookie);
                        vis.execute();
                        messageCount = getMessageCount();
                    }
                    utils.println("Deleted " + initialMessageCount + " message(s).", true);
                }
//                mainView.runButton.setEnabled(true);
            }
        });
        deleteInboxThread.start();
    }

    private ArrayList getMessageIds() {
        ArrayList messsageIds = new ArrayList();
        vis.setURL("https://www.okcupid.com/messages");
        vis.setMethod("GET");
        vis.setSessionCookie(sessionCookie);
        vis.execute();
        this.response = vis.getResponse();

        Pattern p = Pattern.compile("data-threadid=\"([0-9]+)\"");
        Matcher m = p.matcher(this.response);
        while (m.find()) {
            messsageIds.add(m.group(1));
        }
        return messsageIds;
    }

    public int getMessageCount() {
        int messageCount = 0;

        vis.setURL("https://www.okcupid.com/messages");
        vis.setSessionCookie(sessionCookie);
        vis.setMethod("GET");
        utils.print(String.valueOf(vis.getURL()), false);
        vis.execute();
        this.response = vis.getResponse();

        Pattern p = Pattern.compile("storagenumbers[\\Wa-z]+([0-9]+)");
        Matcher m = p.matcher(response);
        if(m.find()) {
            messageCount = Integer.parseInt(m.group(1));
        } else {
            utils.println("\nThe inbox is empty");
        }
        return messageCount;
    }


    public void refreshLogin() {
        utils.println("Refreshing login...");
        isRefreshing = true;
        try {
            if (isLoggedIn) {
                if(!isUsingAlt) {
                    logout();
                    Thread.currentThread().sleep(2000);
                }
                login(username);
            } else {
                login(username);
            }
        } catch (InterruptedException e) {
            utils.println("Failed to refresh login", true);
            e.printStackTrace();
            utils.killAllThreadOccurances(loginThread);
        }
    }

    public Boolean getIsLoggedIn() {
        return isLoggedIn;
    }


    public ArrayList<String> getBannedProfiles() {
        fetchBannedProfiles();
        return bannedProfiles;
    }

    private void fetchBannedProfiles() {
        try {
            _prefs = Preferences.userRoot().node("OkcAccounts/bannedProfiles");
            String[] profiles = _prefs.childrenNames();
            int bannedCount = profiles.length;
            for (int i = 0; i < bannedCount; i++) {
                bannedProfiles.add(profiles[i]);
            }
        } catch (BackingStoreException e) {
            utils.print("Failed to fetch banned account list", true);
            e.printStackTrace();
        }
    }

    public void publishBannedProfile(String profile, String bannedTime, String bannedDate) {
        changeNode("bannedProfiles/" + profile);
        _prefs.put("bannedDate", bannedDate);
        _prefs.put("bannedTime", bannedTime);
        bannedProfiles.add(profile);
    }

    public void setIsRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }

    public String getBannedTime(String bannedProfile) {
        this.bannedTime = fetchBannedTime(bannedProfile);
        return this.bannedTime;
    }

    public String fetchBannedTime(String bannedProfile) {
        changeNode("bannedProfiles/" + bannedProfile);
        String bannedTime = _prefs.get("bannedTime","");
        return bannedTime;
    }

    public String getBannedDate(String bannedProfile) {
        this.bannedDate = fetchBannedDate(bannedProfile);
        return this.bannedDate;
    }

    private String fetchBannedDate(String bannedProfile) {
        changeNode("bannedProfiles/" + bannedProfile);
        String bannedDate = _prefs.get("bannedDate","");
        return bannedDate;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean getAccountLoaded(String profileName) {
        Boolean accountLoaded = null;
        HashMap<String, String> loginSettings = fetchLoginSettings(profileName);
        if(!loginSettings.get("password").isEmpty()) {
            accountLoaded = true;
        } else {
            accountLoaded = false;
        }
        return accountLoaded;
    }

    public void changeProfileText(String profile) {
        Boolean accountLoaded = getAccountLoaded(profile);
        HashMap<String, String> loginSettings = fetchLoginSettings(profile);
        if(accountLoaded) {
//            String username = profile;
//            String password = loginSettings.get("password");
//            String zip = loginSettings.get("zip");
//            String proxy = loginSettings.get("proxy");
//            String port = loginSettings.get("port");

            utils.println("Removing Skype info. from OKC profile, " + profile);
            AccountManager altAccountMgr = new AccountManager(mainView);
            altAccountMgr.login(profile);
//            backupLogin(profile, password, zip, proxy, port);
            vis = new URLVisitor(altAccountMgr);

            vis.setURL("https://www.okcupid.com/profileedit2");
            vis.setMethod("POST");
            vis.setSessionCookie(sessionCookie);
            String formData = "" +
                    "im working a few jobs while attending school for " +
                    "nursing to support myself and my little sister " +
                    "since our parents passed. Weekdays when not in class, " +
                    "I work at starbucks. Weeknights I'm dancing.  " +
                    "And on weekends I bartend alternating between a restaurant and a lounge. " +
                    "So yeah.. not TOO busy lol.";
            vis.setParams("essay_body=" + formData + "&essay_id=1&okc_api=1&authcode=" + authCode);
            utils.print(String.valueOf(vis.getURL()));
            vis.execute();
        } else {
            profileExists = false;
            utils.println("Unable to fetch loginSettings for the requested profile");
        }

    }

    public void setIsUsingAlt(boolean isUsingAlt) {
        this.isUsingAlt = isUsingAlt;
    }

    public boolean getIsUsingAlt() {
        return isUsingAlt;
    }

    public Thread getLoginThread() {
        return loginThread;
    }

    public void publishRunState() {
        runState = mainView.getUiRunState();
        changeNode(account + "/runState");
        Iterator iterator = runState.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = String.valueOf(runState.get(key));

            _prefs.put(key, value);
        }

    }

    public Boolean getLastRunCompleted() {
        return lastRunCompleted;
    }

    public HashMap getRunState() {
        return runState;
    }

    public Preferences getPrefs() {
        return this._prefs;
    }

    public void setLastRunCompleted(boolean lastRunCompleted) {
        this.lastRunCompleted = lastRunCompleted;
    }

    public void unpublishBannedProfile(String bannedProfile) {
        changeNode("bannedProfiles/" + bannedProfile);
        try {
            _prefs.removeNode();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

    }

    public void publishClosedProfile(String profile, String closedTime, String closedDate) {
        changeNode("closedProfiles/" + profile);
        _prefs.put("closedDate", closedDate);
        _prefs.put("closedTime", closedTime);
        closedProfiles.add(profile);
    }

    public ArrayList<String> getClosedProfiles() {
        fetchClosedProfiles();
        return closedProfiles;
    }

    private void fetchClosedProfiles() {
        try {
            _prefs = Preferences.userRoot().node("OkcAccounts/closedProfiles");
            String[] profiles = _prefs.childrenNames();
            int closedCount = profiles.length;
            for (int i = 0; i < closedCount; i++) {
                closedProfiles.add(profiles[i]);
            }
        } catch (BackingStoreException e) {
            utils.print("Failed to fetch closed account list", true);
            e.printStackTrace();
        }
    }

    public Thread getDeleteInboxThread() {
        return deleteInboxThread;
    }
}