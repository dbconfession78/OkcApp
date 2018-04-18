import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stuartkuredjian on 6/21/15.
 */
public class RunManager implements Runnable {
    private Schedule schedule;
    private String searchURL;
    private URLVisitor vis;
    private AccountManager accountMgr;
    private HashMap<String, String> searchSettingsMap = new HashMap<>();
    private SearchMatcher searchMatcher;
    private String sessionCookie;
    private int cycleCount;
    private int visitLimit;
    private double visitDelay;
    private Utilities utils = new Utilities();

    private String response;
    private ArrayList<String> profiles;
    private boolean timerStarted = false;
    private boolean isRunning;
    private int totalVisitsCompleted = 0;
    private int removedCount;
    private Boolean shouldHide;
    private boolean shouldThrow = false;
    private boolean isFreshRun = true;
    private int responseCode = 0;
    private int finalVisitCount = 0;
    private int visitsCompleted;
    private double nextVisit = 0;
    private JLabel nextVisitLabel;
    private Thread nextVisitThread;
    private boolean isVisitingVisitors;
    private HashMap runState = new HashMap();
    private Thread hideProfileThread;
    private boolean runCompleted = false;
    private boolean isPaused = false;
    private boolean isScheduledRun = false;
    private Timer timer;
    private boolean isManualRun;

    public RunManager(AccountManager accountMgr) {
        isScheduledRun = false;
        timerStarted = true;
        isRunning = false;
        this.accountMgr = accountMgr;
        sessionCookie = accountMgr.getSessionCookie();
    }

    public RunManager(Schedule schedule, AccountManager accountMgr) {
        this.schedule = schedule;
        this.accountMgr = accountMgr;

        isScheduledRun = true;
        timerStarted = true;
        isRunning = false;

        sessionCookie = accountMgr.getSessionCookie();
    }

    public void run() {
        timer = new Timer(accountMgr.mainView);
        timer.initTimerThread(this);
        timer.startTimer();

        isRunning = true;
        try {
            this.totalVisitsCompleted = accountMgr.mainView.getTotalVisitsCompleted();

            if(isScheduledRun) {
                String scheduleTitle = schedule.getScheduleTitle();
                searchSettingsMap = this.schedule.fetchScheduleSettings(scheduleTitle);
            } else {
                searchSettingsMap = accountMgr.getSearchSettingsMap();
            }

            if(!isVisitingVisitors) {
                if(!accountMgr.getLastRunCompleted()) {
                    HashMap runState = accountMgr.getRunState();
                    visitsCompleted = Integer.parseInt(String.valueOf(runState.get("visitsCompleted")));
                    visitLimit = Integer.parseInt(searchSettingsMap.get("limit"));
                    visitLimit = visitLimit -  visitsCompleted;
                } else {
                    visitLimit = Integer.parseInt(searchSettingsMap.get("limit"));
                }

                if(visitLimit > 40) {
                    cycleCount = visitLimit / 40;
                } else {
                    cycleCount = 1;
                }
                searchMatcher = new SearchMatcher(accountMgr, searchSettingsMap);
                searchURL = searchMatcher.getSearchURL();


                searchURL = "https://www.okcupid.com/1/apitun/match/search"; // <---------- delete after testing and fixing


                shouldHide = Boolean.valueOf(searchSettingsMap.get("auto_hide"));
                visitDelay = (Double.parseDouble(searchSettingsMap.get("visit_delay")))*1000;
            } else {
                visitLimit = Integer.parseInt(accountMgr.fetchNewVisitorCount());
                JLabel runVisitsToDoUI = accountMgr.mainView.getVisitLimitUI();
                runVisitsToDoUI.setText(String.valueOf("of " + visitLimit));
                cycleCount = 1;
                searchURL = "https://www.okcupid.com/visitors";
                visitDelay = (Double.parseDouble(searchSettingsMap.get("visitDelay")))*1000;
            }

            if(visitLimit > 40) {
                if (visitLimit % 40 != 0) {
                    cycleCount++;
                    finalVisitCount = visitLimit % 40;
                }
            }

            for (int i = 0; i < cycleCount; i++) {
                if(i == cycleCount -1) {
                    if(finalVisitCount > 0) {
                        Pattern p = Pattern.compile("(\"limit\":)(\"[0-9]+\")");
                        Matcher m = p.matcher(searchURL);
                        m.find();
                        String group0 = m.group(0);
                        String group1 = m.group(1);
                        String group2 = m.group(2);

                        searchURL = searchURL.replace(group2, "\"" + String.valueOf(finalVisitCount) + "\"");
                    }
                }

                if (isRunning) {
                    if(isPaused){
                        pauseRun();
                    }
                    if (visitLimit > 15) {
                        if(cycleCount > 1) {
                            if (i == cycleCount / 2) {
                                if (isRunning) {
                                    if(isPaused){
                                        pauseRun();
                                    }
                                    isFreshRun = false;
                                    accountMgr.refreshLogin();
                                    searchMatcher = new SearchMatcher(accountMgr, searchSettingsMap);
                                    searchMatcher.setIsFreshRun(false);
                                }
                            }
                        }
                    }

                    if (this.isRunning) {
                        if(isPaused){
                            pauseRun();
                        }
                        profiles = new ArrayList<>();

                        // get matches
                        if(!isVisitingVisitors) {
                            profiles = fetchMatches(searchURL);
                        } else {
                            profiles = fetchVisitors();
                        }

                        // visit matches
                        visitMatches(profiles);
//                        accountMgr.mainView.setRunsCompleted();
                    }
                }
                else {
                    break;
                }
            }
            if(Boolean.valueOf(searchSettingsMap.get("auto_delete_inbox"))) {
                accountMgr.mainView.runButton.setEnabled(false);
                accountMgr.deleteInbox();
                while(accountMgr.getDeleteInboxThread().isAlive()) {
                    Thread.currentThread().sleep(0);
                }
                accountMgr.mainView.runButton.setEnabled(true);
            }
            this.runCompleted = true;
            this.timerStarted = false;
            this.isRunning = false;
        } catch (Exception e) {
            utils.println("\nException");
            isRunning = false;
            e.printStackTrace();
            if(isScheduledRun) {
                accountMgr.mainView.toggleScheduledRunComponentsOff(schedule.getScheduleTitle());
            } else {
                accountMgr.mainView.toggleManualRunOff();
            }
            utils.stopThread(Thread.currentThread());
        }
    }

    private ArrayList<String> fetchVisitors() {
        ArrayList visitors = new ArrayList();
        URLVisitor vis = new URLVisitor(accountMgr);
        vis.setURL("https://www.okcupid.com/visitors");
        utils.print("Getting Visitor pages...");
        vis.setShouldPrintStatus(false);
        vis.setMethod("GET");
        vis.setSessionCookie(sessionCookie);
        vis.execute();
        String response = vis.getResponse();
//        utils.println(response);

        String pagesString = utils.findString(response, "<a class=\\\"last\\\" href=\\\"/visitors\\?low=[0-9]+\\\">([0-9]+)<", 1);
        int pages = 1;
        if(pagesString != null) {
            pages = Integer.parseInt(pagesString);
        }

        int low = 1;
        for (int i = 0; i < pages; i++) {
            if(i == 0) {

                Pattern p = Pattern.compile("id=\\\"usr-([\\w\\\\ÆÐƎƏƐƔĲŊŒ\u1E9EÞǷȜæðǝəɛɣĳŋœĸſßþƿȝĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţțŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗðéèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîïǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœŔŘŖŚŜŠŞȘṢ\u1E9EŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃẁŵẅƿýỳŷÿȳỹƴźżžẓ±-]+)\\\"");
                Matcher m = p.matcher(response);
                while(m.find() && visitors.size() < visitLimit) {
                    visitors.add(m.group(1));
                }
            }
            if(i >= 1) {
                low = low+25;
                vis = new URLVisitor(accountMgr);
                vis.setURL("https://www.okcupid.com/visitors?low=" + low);
                vis.setMethod("GET");
                vis.setSessionCookie(sessionCookie);
                vis.execute();
                response = vis.getResponse();
//                utils.println(response);

                Pattern p = Pattern.compile("id=\"usr-([\\w\\\\ÆÐƎƏƐƔĲŊŒ\u1E9EÞǷȜæðǝəɛɣĳŋœĸſßþƿȝĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţțŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗðéèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîïǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœŔŘŖŚŜŠŞȘṢ\u1E9EŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃẁŵẅƿýỳŷÿȳỹƴźżžẓ±-]+)\"");
                Matcher m = p.matcher(response);
                while(m.find() && visitors.size() < visitLimit) {
                    visitors.add(m.group(1));
                }
            }
        }
        return visitors;
    }

    private ArrayList<String> fetchMatches(String searchURL) {
        if(isRunning) {
            if(isPaused){
                pauseRun();
            }
            ArrayList<String> profiles = new ArrayList<>();
            shouldThrow = searchMatcher.getShouldThrow();
            if (!shouldThrow) {
                int runVisits = 0;

//                runVisits = Integer.parseInt(utils.findText(searchURL, "\"limit\":\"", "\""))+1;
                runVisits = 41;

                // half second pause
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                vis = new URLVisitor(accountMgr);
                vis.setURL(searchURL);
//                vis.setMethod("GET");
                vis.setMethod("POST");
                vis.setParams("{\"order_by\":\"SPECIAL_BLEND\",\"gentation\":[34],\"gender_tags\":null,\"orientation_tags\":null,\"minimum_age\":26,\"maximum_age\":30,\"locid\":4232451,\"radius\":25,\"lquery\":\"\",\"location\":{\"postal_code\":\"\",\"nameid\":140058,\"display_state\":1,\"locid\":4232451,\"state_code\":\"CA\",\"country_name\":\"United States\",\"longitude\":-12232553,\"popularity\":1239,\"state_name\":\"California\",\"default_radius\":25,\"country_code\":\"US\",\"city_name\":\"San Mateo\",\"density\":10673,\"metro_area\":7360,\"latitude\":3756299},\"located_anywhere\":0,\"last_login\":2678400,\"i_want\":\"women\",\"they_want\":\"men\",\"minimum_height\":null,\"maximum_height\":null,\"languages\":0,\"speaks_my_language\":false,\"ethnicity\":[\"white\"],\"religion\":[],\"availability\":\"single\",\"monogamy\":\"unknown\",\"looking_for\":[\"short_term_dating\",\"long_term_dating\",\"new_friends\"],\"smoking\":[],\"drinking\":[],\"drugs\":[],\"answers\":[],\"interest_ids\":[],\"education\":[],\"children\":[],\"cats\":[],\"dogs\":[],\"tagOrder\":[\"ethnicity\",\"availability\",\"looking_for\"],\"save_search\":true,\"limit\":18,\"fields\":\"userinfo,thumbs,percentages,likes,last_contacts,online\"}");
                vis.setSessionCookie(sessionCookie);
                utils.println("\nGenerating search URL:", false);
                utils.print(String.valueOf(vis.getURL()) + "\n", false);
//                vis.execute();
                while(!vis.getIsConnected()) {
                    vis.execute();
                }
                utils.print("\n");
                if(!isRunning) {
                    utils.println("Run Terminated");
                } else {
                    response = vis.getResponse();
//                    utils.println(response);

                    utils.println("Searching for " + runVisits + " matches...");
                    Pattern p = Pattern.compile("\"username\" : \"([\\w\\\\ÆÐƎƏƐƔĲŊŒ\u1E9EÞǷȜæðǝəɛɣĳŋœĸſßþƿȝĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţțŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗðéèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîïǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœŔŘŖŚŜŠŞȘṢ\u1E9EŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃẁŵẅƿýỳŷÿȳỹƴźżžẓ±-]+)\",");
                    Matcher m = p.matcher(response);
                    Boolean next;
                    int count = 0;

                    while (next = m.find()) {
                        count++;
                        String profile = m.group(1);
                        utils.println(count + ".  " + profile);
                        profiles.add(profile);
                    }
                    if(!shouldHide) {
                        try {
                            removeDuplicates(profiles);
                        } catch (InterruptedException e) {
                            utils.println("InterruptedException");
                            e.printStackTrace();
                        }
                    }
                    utils.println("Found " + profiles.size() + " matches.");
                    return profiles;
                }
            }
        }
        return null;
    }

    private void removeDuplicates(ArrayList<String> profiles) throws InterruptedException {
        removedCount = 0;
        if(searchSettingsMap.get("auto_hide").equals("true")) {
            utils.println("Checking for previously visited profiles...");
            for (int i = 0; i < profiles.size(); ) {
                String profile = profiles.get(i);
                if (hasVisited(profile)) {
                    profiles.remove(profile);
                    removedCount++;
                    continue;
                } else {
                    i++;
                }
            }
        }

        if(removedCount > 0) {
            utils.println("Removed " + removedCount + " previously visited profiles");
        } else {
            utils.println("\nAll profiles are unique");
        }
    }

    private void initNextVisitThread() {
        if(nextVisitThread != null) {
            if(nextVisitThread.isAlive()) {
                utils.killAllThreadOccurances(nextVisitThread);
            }
        }
        nextVisitThread = new Thread(new Runnable() {
            Double visitDelay = Double.valueOf(accountMgr.getSearchSettingsMap().get("visitDelay"));
            double baseCountDown = visitDelay + 1;
            double countDown = baseCountDown;
            @Override
            public void run() {
                Thread.currentThread().setName("Next Visit Thread");
                if(!isRunning) {
                    utils.killAllThreadOccurances(nextVisitThread);
                }
                nextVisitLabel = MainView.getNextVisitLabel();
                nextVisitLabel.setText(String.valueOf(countDown));

                while(countDown > 0 && isRunning) {
//                    utils.println(Double.toString(countDown));
                    if(isPaused){
                        pauseRun();
                    }
                    nextVisitLabel.setText(String.valueOf(countDown));
                    while(countDown == 1.0) {
                        try {
                            Thread.currentThread().sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    countDown = countDown - 1;

                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void visitMatches(ArrayList profiles) {
        runState = accountMgr.getRunState();
        if(!runState.isEmpty()) {
            visitsCompleted = Integer.parseInt(String.valueOf(runState.get("visitsCompleted")));
        }
        try {
            if(isRunning) {
                if(isPaused){
                    pauseRun();
                }
                if(!isVisitingVisitors) {
                    shouldThrow = searchMatcher.getShouldThrow();
                }
                if (!shouldThrow) {
                    if(profiles.size() == 0) {
                        utils.println("\nNo profile matches your search criteria.");
                        isRunning = false;
                    }
                    if(this.isRunning) {
                        if(isPaused){
                             pauseRun();
                        }
                        utils.println("\nVisiting " + profiles.size() + " Matches...");
                        initNextVisitThread();
                        nextVisitThread.start();
                        for (int i = 0; i < profiles.size(); i++) {
                            if(visitsCompleted == Integer.parseInt(String.valueOf(searchSettingsMap.get("limit")))) {
                                isRunning = false;
                                accountMgr.setLastRunCompleted(true);
                            }
                            if (this.isRunning) {
                                if(isPaused){
                                    pauseRun();
                                }
                                String profile = String.valueOf(profiles.get(i));
                                URLVisitor vis = new URLVisitor(accountMgr);
                                vis.setURL("http://www.okcupid.com/profile/" + profile);
                                vis.setMethod("GET");
                                vis.setSessionCookie(sessionCookie);
                                utils.print("\n" + (i + 1) + ".  " + vis.getURL(), false);
                                utils.print("\nVisiting: " + profile + "...  ", true);
                                // Manual Run Thread
                                JLabel nextVisitLabel = MainView.getNextVisitLabel();
                                String nextVisitLabelText = String.valueOf((visitDelay/1000)+1);
                                nextVisitLabel.setText(nextVisitLabelText);

//                                utils.killAllThreadOccurances(nextVisitThread);
                                initNextVisitThread();
                                nextVisitThread.start();

                                vis.execute();

                                response = vis.getResponse();
                                responseCode = vis.getResponseCode();

                                // once a 200 is received inside execute(),
                                // hide profile if the option is set
                                if (responseCode == 200) {
                                    if(!isVisitingVisitors) {
                                        if (searchSettingsMap.get("auto_hide").equals("true")) {
                                            hideProfile(profile);
                                        }
                                    }
                                    logProfile(String.valueOf(profile));
                                    visitsCompleted++;
                                    this.totalVisitsCompleted++;

                                    accountMgr.publishRunState();

                                    accountMgr.mainView.setVisitsCompleted(visitsCompleted);
                                } else {
                                    if(responseCode == 404) {
                                        utils.print(": " + "The profile " + profile + " does not exist!", true);
                                    }
                                }
                                if (this.isRunning) {
                                    if(isPaused){
                                        pauseRun();
                                    }
                                    nextVisit = nextVisit - 1;
                                    Thread.currentThread().sleep((long) visitDelay);
                                }
                            } else {
                                break;
                            }

                        }
                    }
                    utils.println("\nProfiles Visited: " + visitsCompleted, true);
                    if (!isRunning) {
                        utils.println("\nRun Terminated");
                    }

                }
            }
        } catch (InterruptedException e) {
            utils.println("InterruptedException");
            e.printStackTrace();
        }

    }

    public void hideProfile(final String profile) {
        utils.print("Hiding " + profile + "...", true);
        URLVisitor vis = new URLVisitor(accountMgr);
        vis.setSessionCookie(accountMgr.getSessionCookie());
        String authcode = accountMgr.getAuthCode();
        vis.setParams("&access_token=" + authcode);
        vis.setURL("https://www.okcupid.com/apitun/profile/" + profile + "/hide");
        vis.setMethod("POST");
        vis.execute();

    }

    private void logProfile(String profile) {
        BufferedWriter writer = null;
        try {
            File logFile = new File("visited.txt");
            writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.append("\n" + profile);
        } catch (IOException e) {
            utils.println("IOException");
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                utils.println("Exception");
                e.printStackTrace();
            }
        }
    }

    private Boolean hasVisited(String profile) {
        Boolean hasVisited = false;
        try {
            File logFile = new File("visited.txt");
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line = null;
            StringBuffer response = new StringBuffer();
            while((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            reader.close();
            String result = response.toString();

            Pattern p = Pattern.compile(profile);
            Matcher m = p.matcher(result);
            if(m.find()) {
                hasVisited = true;
                utils.println(">>>>>>>Exclude: " + m.group(0));
            }
        } catch (Exception e) {
            utils.println("Exception");
            e.printStackTrace();
        }
        return hasVisited;
    }

    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public int getTotalVisits() {
        return this.totalVisitsCompleted;
    }

    public boolean getIsFreshRun() {
        return isFreshRun;
    }

    public void setIsFreshRun(boolean isFreshRun) {
        this.isFreshRun = isFreshRun;
    }

    public void setTotalVisitsCompleted(int totalVisitsCompleted) {
        this.totalVisitsCompleted = totalVisitsCompleted;
    }

    public void setIsVisitingVisitors(boolean isVisitingVisitors) {
        this.isVisitingVisitors = isVisitingVisitors;
    }

    public boolean getRunCompleted() {
        return runCompleted;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }


    public void pauseRun() {
        isPaused = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                timer.setIsPaused(true);
                Thread.currentThread().setName("Pause Thread");
            }
        }).start();

        // Manual Run Thread
        while(isPaused) {
            try {
                // Manual Run Thread
                Thread.currentThread().sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timer.setIsPaused(false);
    }

    public void resumeRun() {
        isPaused = false;
        timer.setIsPaused(false);
        utils.println("\n Run Resumed", true);
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public void setRunCompleted(boolean runCompleted) {
        this.runCompleted = runCompleted;
    }

    public void setIsManualRun(boolean isManualRun) {
        this.isManualRun = isManualRun;
    }
    
    public Boolean getIsManualRun() {
        return isManualRun;
    }

    public void setIsScheduledRun(boolean isScheduledRun) {
        this.isScheduledRun = isScheduledRun;
    }

    public Boolean getIsScheduledRun() {
        return isScheduledRun;
    }
}
