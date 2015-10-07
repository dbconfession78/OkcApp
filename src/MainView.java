import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Tue May 19 15:21:50 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
class MainView extends JFrame {
    private static String selectedMenuItemText = "";
    private Timer timer = new Timer(this);
    private Thread timerThread;
    private Boolean isRunning = false;
    public AccountManager accountMgr;
    private ConsoleView consoleView = new ConsoleView(this);
    public JButton addAccountButton = new JButton();
    private ManageAccountsView manageAccountsView;
    private Utilities utils;
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private String strSeconds;
    private String strMinutes;
    private String strHours;
    private int totalSeconds = 0;
    private int totalMinutes = 0;
    private int totalHours = 0;
    private String strTotalSeconds;
    private String strTotalMinutes;
    private String strTotalHours;
    private Thread manualRunThread;
    private int totalVisitsCompleted = 0;
    public RunManager runManager;
    private int totalRuns = 0;
    private ArrayList<String> watchedProfiles;
    private boolean isWatching = false;
    private Thread autoWatchThread;
    private HashMap<String, String> searchSettingsMap;
    private Boolean isLoggedIn = false;
    private ArrayList<String> bannedProfiles;
    private ArrayList<String> closedProfiles;
    private int watcherCount = 0;
    private boolean isVisible = false;
    private int checkFrequency = 0;
    private static ArrayList<Schedule> schedules = new ArrayList<>();

    private JFrame consoleFrame = new JFrame("Console");
    private Boolean consoleVisible = false;
    private Thread countDownThread;
    private Thread visitorCountThread;

    private ArrayList<Thread> autoWatchThreads = new ArrayList<>();
    private int threadCount = 0;

    private String completedVisits;
    private int requestedVisits;
    private AccountManager altAccountMgr;
    private ArrayList outputArray;
    private SearchPreferencesView searchPreferencesView;
    private Boolean lastRunCompleted = true;
    private boolean shouldRepeat = false;
    private String username;
    private String account;
    private Preferences _prefs;
    private HashMap uiRunState;
    private static boolean schedIsActive = false;
    private Thread scheduleThread;
    private Boolean isSchedule = false;
    private Schedule schedule;
    private boolean manualRunActive = false;

    public MainView() {
        outputArray = OkcApp.getOutputArray();
        initComponents();
        onLoad();
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }


    public void toggleConsole() {
        if(!consoleFrame.isVisible()) {
            setConsoleLocation();
            consoleFrame.setVisible(true);
        } else {
            consoleFrame.setVisible(false);
        }
    }

    private void setConsoleLocation() {
        Rectangle mainBounds = panel1.getParent().getBounds();
        Point mainLocation = panel1.getParent().getLocationOnScreen();
        int x = mainLocation.x;
        int y = (int) (mainLocation.y+mainBounds.getHeight()+25);
        consoleFrame.setLocation(x + 1, y);
    }

    private void initConsolePanel() {
        consoleFrame.setResizable(false);
        JPanel consolePanel1 = consoleView.getPanel1();
        consoleFrame.setContentPane(consolePanel1);
        consoleFrame.pack();
    }

    public static JLabel getNextVisitLabel() {
        return nextVisitLabel;
    }

    private void onLoad() {
        initConsolePanel();
        accountMgr = new AccountManager(this);
        populateScheduleMenu();

        addAccountButton.setText("Add Account");
        addAccountButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        leftPanel.add(addAccountButton, CC.xy(1, 1));
        addAccountButton.setVisible(false);
        addAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                accountMgr.showAddAccountView();
            }
        });

        addAccountButton.addPropertyChangeListener("visible", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                addAccountButtonPropertyChange(evt);
            }
        });


        searchPreferencesView = new SearchPreferencesView();
        runManager = new RunManager(accountMgr);
        int accountCount = accountMgr.fetchAccountCount();
        if(accountCount == 0) {
            manageAccountsMenuItem.setEnabled(false);
        }
        this.utils = new Utilities();
        initAutoWatchThread();
    }

    public void populateScheduleMenu() {

        // remove all schedules from schedule menu before re-populating it
        Component[] components = scheduleMenu.getMenuComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            String componentName = component.getName();
            if(componentName != null) {
                if (componentName.startsWith("schedule")) {
                    scheduleMenu.remove(component);
                }
            }
        }

        final ArrayList schedTitles = ScheduleView.fetchSchedTitles();
        Component[] schedulePanels = activeSchedulePanel.getComponents();
        for (int i = 0; i < schedTitles.size(); i++) {
            //---- scheduleMenuItem ----
            final String schedTitle = String.valueOf(schedTitles.get(i));
            Schedule schedule = new Schedule(this, schedTitle);
            schedules.add(schedule);
            scheduleMenu.add(schedule.getScheduleMenuItem());
            JPanel schedulePanel = (JPanel) schedulePanels[i];
            populateActiveSchedulePanel(schedule, schedulePanel);
        }
    }

    public void scheduleMenuItemActionPerformed(ActionEvent e, String scheduleTitle) {
        toggleScheduleListener(scheduleTitle);

    }

    private void toggleScheduleListener(String scheduleTitle) {
        for (int i = 0; i < schedules.size(); i++) {
            schedule = schedules.get(i);
            if(schedule.getScheduleTitle().equals(scheduleTitle)) {
                if(!schedule.getIsActivated()) {
                    schedules.get(i).setIsActivated(true);
                    activateScheduleListener(scheduleTitle);
                } else {
                    schedules.get(i).setIsActivated(false);
                    deactivateScheduleListener(scheduleTitle);
                }
                break;
            }
        }
    }

    private void populateActiveSchedulePanel(Schedule schedule, JPanel jPanel) {
        Component[] components = jPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            JLabel jLabel = (JLabel) component;
            String labelName = jLabel.getName();
            if(jLabel.getText().equals("-")) {
                continue;
            }
            if(labelName.startsWith(("scheduleName"))) {
                if(jLabel.getText().equals(""))   {
                    jLabel.setText(schedule.getScheduleTitle());
                } else {
                    break;
                }
                continue;
            }

            if(labelName.startsWith("scheduleTime")) {
                jLabel.setText(schedule.getSearchSettingsMap().get("startTime"));
            }
        }
    }

    private void deactivateScheduleListener(String scheduleTitle) {
        schedule.setIsActivated(false);
        toggleScheduleMenuItemPropertiesOff(scheduleTitle);
        Component[] components = activeSchedulePanel.getComponents();

        // get each schedulePanel one at a time
        for (int i = 0; i < components.length; i++) {
            JPanel schedulePanel = (JPanel) components[i];
            Component[] scheduleLabels = schedulePanel.getComponents();

            //get the first label in the panel
            for (int j = 0; j < scheduleLabels.length; j++) {

                // if it's text equals scheduleTitle
                // make the panel visible
                JLabel scheduleLabel = (JLabel) scheduleLabels[j];
                if(scheduleLabel.getText().equals(scheduleTitle)) {
                    schedulePanel.setVisible(false);
                }
                // break regardless of match since only
                // first label in the panel matters
                break;
            }
        }
    }

    private void activateScheduleListener(String scheduleTitle) {
        toggleScheduleMenuItemPropertiesOn(scheduleTitle);
        Component[] components = activeSchedulePanel.getComponents();

        // get each schedulePanel one at a time
        for (int i = 0; i < components.length; i++) {
            JPanel schedulePanel = (JPanel) components[i];
            Component[] scheduleLabels = schedulePanel.getComponents();

            //get the first label in the panel
            for (int j = 0; j < scheduleLabels.length; j++) {

                // if it's text equals scheduleTitle
                // make the panel visible
                JLabel scheduleLabel = (JLabel) scheduleLabels[j];
                if(scheduleLabel.getText().equals(scheduleTitle)) {
                    schedulePanel.setVisible(true);
                }
                // break regardless of match since only
                // first label in the panel matters
                break;
            }
        }
        schedule.run();
    }

    private Schedule getSchedule(String scheduleTitle) {
        for (int i = 0; i < schedules.size(); i++) {
            Schedule schedule = schedules.get(i);
            String scheduleTitle2 = schedule.getScheduleTitle();
            if(scheduleTitle.equals(scheduleTitle2)) {
                return schedule;
            }
        }
        return null;
    }

    private void addAccountButtonPropertyChange(PropertyChangeEvent evt) {
        if(addAccountButton.isVisible()) {
            manageAccountsMenuItem.setEnabled(false);
        } else {
            Boolean isLoggedIn = accountMgr.getIsLoggedIn();
            if(!isLoggedIn) {
                manageAccountsMenuItem.setEnabled(true);
                clearAccountsMenuItem.setEnabled(false);
            } else {
                manageAccountsMenuItem.setEnabled(false);
                clearAccountsMenuItem.setEnabled(true);
            }
        }
    }

    private void quitMenuItemActionPerformed(ActionEvent e) {
        System.exit(0);
    }

    public int getTotalVisitsCompleted() {
        return this.totalVisitsCompleted;
    }

    private void manageAccountsMenuItemActionPerformed(ActionEvent e) {
        manageAccountsView = accountMgr.getManageAccountsView();
        accountMgr.populateUsernames(manageAccountsView.usernameCombo);
        manageAccountsView.setVisible(true);
    }

    private void addAccountMenuItemActionPerformed(ActionEvent e) {
        accountMgr.showAddAccountView();
    }

    private void loginButtonActionPerformed(ActionEvent e) {
        isLoggedIn = accountMgr.getIsLoggedIn();
        if(!isLoggedIn) {
            String username = usernamesCombo.getSelectedItem().toString();
            accountMgr.login(username);
        } else {
            clearWatchListPanel();
            accountMgr.logout();
            resetRunMetrics();
        }
    }

    private void initVisitorCountThread() {
        visitorCountThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Visitor Count Thread");
                while(isLoggedIn) {
                    updateVisitorCountUI();
                    try {
                        Thread.currentThread().sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateVisitorCountUI() {
        String visitorCount = accountMgr.fetchNewVisitorCount();
        visitorCountLabel.setText(visitorCount);
    }

    public void toggleRestrictedComponents() {
        isLoggedIn = accountMgr.getIsLoggedIn();
        if(isLoggedIn) {
            enableScheduleMenuItems(true);
            initVisitorCountThread();
            visitorCountThread.start();
//            loginButton.setText("Logout");
            runButton.setEnabled(true);
            editButton.setEnabled(true);
//            usernamesCombo.setEnabled(false);
            clearAccountsMenuItem.setEnabled(false);
            visitProfileMenuItem.setEnabled(true);
            checkProfileMenuItem.setEnabled(true);
            if(!isWatching) {
                editAutoWatchListMenuItem.setEnabled(true);
            }
            watchedProfiles = accountMgr.fetchWatchers();
            watcherCount = watchedProfiles.size();
            if(watcherCount > 0) {
                autoWatchButton.setEnabled(true);
            }
            manageAccountsMenuItem.setEnabled(false);
            addAccountMenuItem.setEnabled(false);
            populateSettings(accountMgr.getSearchSettingsMap());
            resetIndexMenuItem.setEnabled(false);
            deleteInboxMenuItem.setEnabled(true);
            visitVisitorsMenuItem.setEnabled(true);
        } else {
            enableScheduleMenuItems(false);
            usernamesCombo.setEnabled(true);
            loginButton.setText("Login");
            runButton.setEnabled(false);
            editButton.setEnabled(false);
            manageAccountsMenuItem.setEnabled(true);
            clearAccountsMenuItem.setEnabled(true);
            visitProfileMenuItem.setEnabled(false);
            checkProfileMenuItem.setEnabled(false);
            editAutoWatchListMenuItem.setEnabled(false);
            if(isWatching) {
                toggleAutoWatch();
            }
            autoWatchButton.setEnabled(false);
            manageAccountsMenuItem.setEnabled(true);
            addAccountMenuItem.setEnabled(true);
            resetIndexMenuItem.setEnabled(true);
            deleteInboxMenuItem.setEnabled(false);
            visitVisitorsMenuItem.setEnabled(false);
        }
    }

    private void enableScheduleMenuItems(boolean shouldEnable) {
        Component[] menuComponents = scheduleMenu.getMenuComponents();
        for (int i = 0; i < menuComponents.length; i++) {
            Component component = menuComponents[i];
            String componentClassName = component.getClass().getName();
            String componentName = component.getName();
            if(componentName.startsWith("schedule")) {
                if(componentClassName.equals("javax.swing.JMenuItem")) {
                    JMenuItem jMenuItem = (JMenuItem) component;
                    jMenuItem.setEnabled(shouldEnable);
                }
            }
        }
    }

    public void toggleScheduledRunComponentsOn(String scheduleTitle) {
        loginButton.setEnabled(false);
        stopButton.setVisible(true);
        editButton.setEnabled(false);

        runButton.setText("Pause");
        String schedFolder = Schedule.fetchSchedFolderBySchedTitle(scheduleTitle);
        _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
        String visitLimit = _prefs.get("limit", "no value");
        visitLimitUI.setText("of " + visitLimit);
        startTimeUI.setText(getCurrentTime());
        endTimeUI.setText(scheduleTitle);
        endTimeUI.setForeground(Color.BLUE);

        Component[] components = activeSchedulePanel.getComponents();

        // get each schedulePanel one at a time
        for (int i = 0; i < components.length; i++) {
            JPanel schedulePanel = (JPanel) components[i];
            Component[] scheduleLabels = schedulePanel.getComponents();

            //get the first label in the panel
            for (int j = 0; j < scheduleLabels.length; j++) {

                // if it's text equals scheduleTitle
                // make the panel visible
                JLabel scheduleLabel = (JLabel) scheduleLabels[j];
                if(scheduleLabel.getText().equals(scheduleTitle)) {
                    Color color = new Color(3, 101, 6);
                    scheduleLabel.setForeground(color);
                    JLabel scheduleLabel2 = (JLabel) scheduleLabels[j+1];
                    scheduleLabel2.setForeground(color);
                    JLabel scheduleLabel3 = (JLabel) scheduleLabels[j+2];
                    scheduleLabel3.setForeground(color);
                    break;
                } else {
                    continue;
                }
            }
        }
    }

    public void toggleScheduleMenuItemPropertiesOn(String scheduleTitle) {
        Component[] menuComponents = scheduleMenu.getMenuComponents();
        for (int i = 0; i < menuComponents.length; i++) {
            Component component = menuComponents[i];
            String componentClassName = component.getClass().getName();
            String componentName = component.getName();
            if(componentClassName.equals("javax.swing.JMenuItem")) {
                if(componentName.startsWith("schedule")) {
                    JMenuItem jMenuItem = (JMenuItem) component;
                    if(jMenuItem.getText().equals(scheduleTitle)) {
                        jMenuItem.setForeground(Color.BLUE);
                        jMenuItem.setText(scheduleTitle + " - Active");
                        break;
                    }
                }
            }
        }
    }

    private void toggleScheduleMenuItemPropertiesOff(String scheduleTitle) {
        Component[] menuComponents = scheduleMenu.getMenuComponents();
        for (int i = 0; i < menuComponents.length; i++) {
            Component component = menuComponents[i];
            String componentClassName = component.getClass().getName();
            String componentName = component.getName();
            if(componentClassName.equals("javax.swing.JMenuItem")) {
                if(componentName.startsWith("schedule")) {
                    JMenuItem jMenuItem = (JMenuItem) component;
                    if(jMenuItem.getText().startsWith(scheduleTitle)) {
                        jMenuItem.setForeground(Color.BLACK);
                        jMenuItem.setText(String.valueOf(scheduleTitle));
                        break;
                    }
                }
            }
        }
    }

    private void runButtonActionPerformed(ActionEvent e) {
        if (runButton.getText().equals("Pause")) {
            pauseRun();
        } else if (runButton.getText().equals("Run")) {
            lastRunCompleted = accountMgr.getLastRunCompleted();
            seconds = Integer.parseInt(elapsedUI.getText().substring(6));
            minutes = Integer.parseInt(elapsedUI.getText().substring(3,5));
            hours = Integer.parseInt(elapsedUI.getText().substring(0,2));
            initManualRunThread(false);
            toggleManualRunOn();
        } else if (runButton.getText().equals("Resume")) {
            resumeRun();
        }
    }

    private void resumeRun() {
        if(runManager.getIsManualRun()) {
            runManager.setIsPaused(false);
            utils.println("\nManual Run Resumed", true);
        } else {
            schedule.getRunManager().setIsPaused(false);
            utils.println("\nSchedule Run Resumed", true);
        }

        runButton.setText("Pause");
        endTimeUI.setText("RUNNING");
        timer.setIsPaused(false);
    }

    private void pauseRun() {
        if(manualRunThread != null && manualRunThread.isAlive()) {
            try {
                runManager.setIsPaused(true);
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            utils.println("\nManual Run Paused", true);
        } else if (schedule.getIsActivated()) {
            schedule.getRunManager().setIsPaused(true);
            utils.println("\nSchedule Run Paused", true);
        }
        runButton.setEnabled(true);
        runButton.setText("Resume");
        endTimeUI.setText("PAUSED");
    }


    private void toggleManualRunOn() {
        toggleManualRunComponentsOn();
        System.out.println(manualRunThread.getName());
        manualRunThread.start();
    }

    public void toggleManualRunOff() {
        runManager.setIsManualRun(false);
        toggleManualRunComponentsOff();
        runManager.setIsRunning(false);
    }

    private void toggleManualRunComponentsOff() {
        loginButton.setEnabled(true);
        stopButton.setVisible(false);
        isRunning = false;
        runManager.setRunCompleted(false);
        setEndTime(getCurrentTime());
        runButton.setEnabled(true);
        runButton.setText("Run");
        resetRunButton.setEnabled(true);
        resetTotalButton.setEnabled(true);
        editButton.setEnabled(true);
        accountMgr.publishRunState();
    }

    private void toggleManualRunComponentsOn() {
        loginButton.setEnabled(false);

        runButton.setText("Pause");
        if (lastRunCompleted) {
            String currentTime = getCurrentTime();
            startTimeUI.setText(currentTime);
            setVisitsCompleted(0);
            resetRunElapsed();
        }

        visitLimitUI.setText("of " + accountMgr.getSearchSettingsMap().get("limit"));
        endTimeUI.setForeground(Color.BLUE);
        endTimeUI.setText("RUNNING");
        runManager.setIsFreshRun(true);

        stopButton.setVisible(true);
        resetRunButton.setEnabled(false);
        resetTotalButton.setEnabled(false);
        editButton.setEnabled(false);
    }

    private void resetRunElapsed() {
        hours = 0;
        minutes = 0;
        seconds = 0;
        elapsedUI.setText("00:00:00");
    }

    private void initManualRunThread(final Boolean isVisitingVisitors) {
        manualRunThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Manual Run Thread");
                System.out.println(manualRunThread.getName());
                utils.println("Starting run...");
                searchSettingsMap = accountMgr.getSearchSettingsMap();
                lastRunCompleted = accountMgr.getLastRunCompleted();
                if(lastRunCompleted) {
                    totalVisitsCompleted = runManager.getTotalVisits();
                } else {
                    totalVisitsCompleted = Integer.parseInt(String.valueOf(accountMgr.getRunState().get("visitsCompleted")));
                }

                runManager = new RunManager(accountMgr);
                runManager.setIsManualRun(true);
                runManager.setIsRunning(true);
                runManager.setIsVisitingVisitors(isVisitingVisitors);
                runManager.run();
                toggleManualRunOff();
            }
        });
    }

    private void setEndTime(String time) {
        endTimeUI.setForeground(Color.BLACK);
        endTimeUI.setText(time);
    }

    private void editButtonActionPerformed(ActionEvent e) {
        SearchPreferencesView searchPreferencesView = new SearchPreferencesView(accountMgr);
        if(!searchPreferencesView.isVisible()) {
            searchPreferencesView.setVisible(true);
            utils.populateComponents(searchPreferencesView.panel1, accountMgr.getSearchSettingsMap());
            runButton.setFocusable(false);
        }
    }

    public void populateSettings(HashMap<String, String> searchSettings) {
        String autoHide = searchSettings.get("autoHide");
        String orientation = searchSettings.get("orientation");
        String radius = searchSettings.get("radius");
        String minAge = searchSettings.get("minimum_age");
        String lastOnline = searchSettings.get("last_login");
        String orderBy = searchSettings.get("order_by");

        if(watch1.getText().equals("")) {
            populateWatchListPanel(watchedProfiles);
        }

        if(autoHide.equals("true")) {
            this.autoHideLabel.setText("- Auto-hide: on");
        } else {
            this.autoHideLabel.setText("- Auto-hide: off");
        }

        if(orientation.equals("")) {
            this.orientation.setText("-");
        } else {
            this.orientation.setText("- " + orientation);
        }

        if(radius.equals("Anywhere") || radius.equals("")) {
            this.radius.setText("- Anywhere");
        } else {
            this.radius.setText("- within " + searchSettings.get("radius") + " miles");
        }

        if(minAge.equals("")) {
            this.age.setText("- Age: 18 - 99");
        } else {
            age.setText("- Age: " + searchSettings.get("minimum_age") + " - " + searchSettings.get("maximum_age"));
        }

        if(lastOnline.equals("")) {
            this.lastOnline.setText("-");
        } else {
            if (!searchSettings.get("last_login").equals("online now")) {
                this.lastOnline.setText("- online in the " + searchSettings.get("last_login"));
            } else {
                this.lastOnline.setText("- online now");
            }
        }

        if(orderBy.equals("")) {
            this.orderBy.setText("-");
        } else {
            this.orderBy.setText("- sorted by " + searchSettings.get("order_by"));
        }
    }

    public void populateWatchListPanel(ArrayList<String> watchedProfiles) {
        clearWatchListPanel();
        watchedProfiles = accountMgr.fetchWatchers();
        int numWatchers = watchedProfiles.size();
        if(numWatchers > 0) {
            autoWatchButton.setEnabled(true);
        } else {
            autoWatchButton.setEnabled(false);
        }
        Component[] components = watchListPanel.getComponents();
        for (int i = 0; i < numWatchers; i++) {
            for (int j = 0; j < components.length; j++) {
                Component component = components[j];
                if(!component.isVisible()) {
                    component.setVisible(true);
                    JLabel jLabel = (JLabel) component;
                    jLabel.setText(watchedProfiles.get(i));
                    jLabel.setForeground(Color.BLACK);
                    components[j+1].setForeground(Color.BLACK);
                    JLabel statusLabel = (JLabel) components[j+1];
                    statusLabel.setText("-");
                    components[j+1].setVisible(true);
                    break;
                }
            }
        }

    }

    private void clearWatchListPanel() {
        Component[] components = watchListPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            String componentClassName = component.getClass().getName();
            if(component.isVisible()) {
                if (componentClassName.equals("javax.swing.JLabel")) {
                    JLabel jLabel = (JLabel) component;
                    jLabel.setText("");
                    jLabel.setVisible(false);
                    Component component2 = components[i + 1];
                    component2.setVisible(false);
                }
            }
        }
    }

//    public void setRunsCompleted() {
//        totalRuns++;
//    }

    public void setVisitsCompleted(int runVisitsCompleted) {
        this.visitsCompletedUI.setText(String.valueOf(runVisitsCompleted));
        this.totalVisitsUI.setText(String.valueOf(runManager.getTotalVisits()));
    }

    private void resetTotalButtonActionPerformed(ActionEvent e) {
        // current
        hours = 0;
        minutes = 0;
        seconds = 0;
        visitsCompletedUI.setText("0");
        elapsedUI.setText("00:00:00");

        // Total
        totalHours = 0;
        totalMinutes = 0;
        totalSeconds = 0;
        totalRuns = 0;
        totalVisitsCompleted = 0;
        runManager.setTotalVisitsCompleted(totalVisitsCompleted);
        totalVisitsUI.setText(String.valueOf(totalVisitsCompleted));
    }

    public JComboBox getUsernamesCombo() {
        return this.usernamesCombo;
    }

    private void clearAccountsMenuItemActionPerformed(ActionEvent e) {
        accountMgr.clearAccounts();
        accountMgr.populateUsernames(usernamesCombo);
        boolean oldVisible = !addAccountButton.isVisible();
        boolean newVisible = addAccountButton.isVisible();
        addAccountButton.firePropertyChange("visible", oldVisible, newVisible);
        clearAccountsMenuItem.setEnabled(false);
    }

    private void resetIndexActionPerformed(ActionEvent e) {
        accountMgr.resetIndex();
    }

    private void runButtonPropertyChange(PropertyChangeEvent e) {
        Boolean isLoggedIn = accountMgr.getIsLoggedIn();
        if(isLoggedIn) {
            loginButton.setEnabled(true);
        } else {
            loginButton.setEnabled(false);
        }
    }

    private void usernamesComboPropertyChange(PropertyChangeEvent e) {
        if(usernamesCombo.getItemCount() == 0) {
            accountMgr.setUsernameComponent(usernamesCombo, addAccountButton);
            clearAccountsMenuItem.setEnabled(true);
        }
    }

    private void visitProfileMenuItemActionPerformed(ActionEvent e) {
        CheckProfileView checkProfileView = new CheckProfileView(accountMgr, true);
        checkProfileView.setSingleCheck(true);
        checkProfileView.setVisible(true);
    }

    private void checkProfileMenuItemActionPerformed(ActionEvent e) {
        CheckProfileView checkProfileView = new CheckProfileView(accountMgr, false);
        checkProfileView.setIsWatching(false);
        checkProfileView.setSingleCheck(true);
        checkProfileView.setVisible(true);
    }

    private void resetRunButtonActionPerformed(ActionEvent e) {
        resetRunMetrics();
        accountMgr.setLastRunCompleted(true);
        accountMgr.publishRunState();
    }

    public void resetRunMetrics() {
        resetVisits();
        resetStartTime();
        resetRunElapsed();
        resetEndTime();
    }

    private void resetEndTime() {
        setEndTime("00:00");
    }

    private void resetStartTime() {
        setStartTime("00:00");
    }

    private void resetVisits() {
        setVisitsCompleted(0);
    }

    private void initAutoWatchThread() {
        autoWatchThreads.add(
                autoWatchThread = new Thread() {
                    @Override
                    public void run() {
                        Thread.currentThread().setName("Auto-Watch Thread");
                        autoWatchThread.setName("AutoWatchThread-" + threadCount);
                        checkFrequency = 60;
                        int incFactor = 0;
                        CheckProfileView checkProfileView = new CheckProfileView(accountMgr, false);
                        utils.println("Auto-watch engaged...");
                        initCountDownThread();
                        countDownThread.start();
                        while (isWatching) {
                            utils.println("Auto-watch: " + (checkFrequency * incFactor) + " seconds", false);
                            incFactor++;
                            if (incFactor % 50 == 0) {
                                AccountManager refreshAccountMgr;
                                if (altAccountMgr != null) {
                                    refreshAccountMgr = altAccountMgr;
                                } else {
                                    refreshAccountMgr = accountMgr;
                                }
                                refreshAccountMgr.setIsRefreshing(true);
                                refreshAccountMgr.refreshLogin();
                            }
                            checkProfileView.setIsWatching(true);
                            checkProfiles(checkProfileView);
                            try {
                                if (autoWatchThreads.size() > 1) {
                                    autoWatchThreads.get(autoWatchThreads.size() - 2).stop();
                                    autoWatchThreads.remove(autoWatchThreads.size() - 2);
                                }
                                Thread.currentThread().sleep(checkFrequency * 1000);
                            } catch (InterruptedException e) {
                                utils.println("Auto-watch interrupted... Re-engaging.");
                                initAutoWatchThread();
                                autoWatchThread.start();
                            }
                            if (!isWatching) {
                                while (!isWatching) {
                                    try {
                                        Thread.currentThread().sleep(1000);
                                    } catch (InterruptedException e) {
                                        utils.println("InterruptedException");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        while (!isWatching) {
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                utils.println("InterruptedException");
                                e.printStackTrace();
                            }
                            if (isWatching) {
                                while (isWatching) {
                                    checkProfiles(checkProfileView);
                                    for (int i = 0; i < checkFrequency; i++) {
                                        try {
                                            Thread.currentThread().sleep(1000);
                                        } catch (InterruptedException e) {
                                            utils.println("InterruptedException");
                                            e.printStackTrace();
                                        }
                                        if (!isWatching) {
                                            utils.println("Auto-Watch Cancelled");
                                            initAutoWatchThread();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        );
        threadCount++;
    }

    private void initCountDownThread() {
        countDownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Count-down Thread");
                int nextCheck = checkFrequency;
                while(isWatching) {
                    nextCheckLabel.setText(String.valueOf(nextCheck));
                    if (nextCheck == 0) {
                        nextCheck = checkFrequency;
                    } else {
                        nextCheck = nextCheck - 1;
                    }
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void checkProfiles(CheckProfileView checkProfileView) {
        watchedProfiles = new ArrayList<>();
        watchedProfiles = accountMgr.fetchWatchers();

        for (int i = 0; i < watchedProfiles.size(); i++) {
            checkProfileView.setShouldVisit(false);
            checkProfileView.checkProfile(watchedProfiles.get(i));
                Component[] components = watchListPanel.getComponents();
                for (int j = 0; j < components.length; j++) {
                    Component component = components[j];
                    String componentClassName = component.getClass().getName();
                    if(componentClassName.equals("javax.swing.JLabel")) {
                        JLabel jLabel = (JLabel) component;
                        if(jLabel.getText().equals(watchedProfiles.get(i))) {
                            JLabel jLabel2 = (JLabel) components[j+1];
                            isVisible = checkProfileView.getIsVisible();
                            String profile = jLabel.getText();
                            // If profile is already on the closed list, skip it
                            if(closedProfiles.contains(profile)) {
                                continue;
                            }
                            if(isVisible) {
                                // if profile is visible report OK
                                Color color = new Color(3, 101, 6);
                                jLabel.setForeground(color);
                                jLabel2.setForeground(color);
                                jLabel2.setText("OK");
                                // check to see if account was previously banned
                                if(bannedProfiles.contains(profile)) {
                                    // if it was previously added to the banned list,
                                    // remove it and send email/text
                                    bannedProfiles.remove(profile);
                                    accountMgr.unpublishBannedProfile(profile);
                                    String[] emailRecipients = new String[]{"sgkur04@gmail.com", "9736150121@vtext.com", "lshamdan90@gmail.com", "3364202797@vtext.com"};
                                    utils.notify(
                                            emailRecipients,
                                            "OkcApp Auto-Notification",
                                            "The profile " + profile + " has become visible again.");
                                }
                            } else {
                                // if profile is not visible
                                // check to see if it's on the banned list already
                                if(bannedProfiles.contains(profile)) {
                                    // if the profile already exists on the banned list

//                                    checkProfileView.checkProfile(profile);
//                                    isVisible = checkProfileView.getIsVisible();

//                                    if(isVisible) {
//                                        // If profile is no longer banned, report OK
//                                        Color color = new Color(3, 101, 6);
//                                        jLabel.setForeground(color);
//                                        jLabel2.setForeground(color);
//                                        jLabel2.setText("OK");
//                                        String[] emailRecipients = new String[]{"sgkur04@gmail.com", "9736150121@vtext.com", "lshamdan90@gmail.com", "3364202797@vtext.com"};
//                                        utils.notify(
//                                                emailRecipients,
//                                                "OkcApp Auto-Notification",
//                                                "The profile " + profile + " has become visible again.");
//                                    } else {
                                        // if account is still banned,
                                        // check to see if it still exists.
                                        checkProfileView.setShouldVisit(true);
                                        checkProfileView.checkProfile(profile);
                                        Boolean profileActive = checkProfileView.getProfileActive();
                                        if(profileActive) {
                                            // if account does still exist
                                            // report time or date of ban
                                            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
                                            Date date = new Date();
                                            String today = dateFormat.format(date);
                                            String bannedTime = accountMgr.getBannedTime(profile);
                                            String bannedDate = accountMgr.getBannedDate(profile);
                                            if (today.equals(bannedDate)) {
                                                jLabel2.setText(bannedTime);
                                            } else {
                                                jLabel2.setText(bannedDate);
                                            }

                                            jLabel.setForeground(Color.RED);
                                            jLabel2.setForeground(Color.RED);
                                        } else {
                                            // If account does not exist,
                                            // report it as closed,
                                            // send email/text, and
                                            // add/publish to closed list
                                            jLabel2.setText("Closed");
                                            jLabel.setForeground(Color.RED);
                                            jLabel2.setForeground(Color.RED);
                                            String[] emailRecipients = new String[]{"sgkur04@gmail.com", "9736150121@vtext.com", "lshamdan90@gmail.com", "3364202797@vtext.com"};
                                            utils.notify(
                                                    emailRecipients,
                                                    "OkcApp Auto-Notification",
                                                    "The profile " + profile + " has been closed.");
                                            publishClosedProfile(profile);
                                            // TODO: implement createNewProfile()
                                        }
//                                    }
                                } else {
                                    // if account is banned but not on the banned list
                                    // check to see if it still exists
                                    checkProfileView.setShouldVisit(true);
                                    checkProfileView.checkProfile(profile);
                                    Boolean profileActive = checkProfileView.getProfileActive();
                                    Boolean accountLoaded = accountMgr.getAccountLoaded(profile);
                                    if(profileActive) {
                                        // if the account still exists
                                        if (accountLoaded) {
                                            publishBannedProfile(jLabel2, profile);
                                            String[] emailRecipients = new String[]{"sgkur04@gmail.com", "9736150121@vtext.com", "lshamdan90@gmail.com", "3364202797@vtext.com"};
                                            utils.notify(
                                                    emailRecipients,
                                                    "OkcApp Auto-Notification",
                                                    "The profile " + profile + " is not visible.");
                                            jLabel.setForeground(Color.RED);
                                            jLabel2.setForeground(Color.RED);
                                        } else {
                                            // in the event that an account isn't visible,
                                            // login credentials for the account in question are required to see if it is still active
                                            utils.println("Please add the account " + profile + " before attempting to Auto-Watch.");
                                        }
                                    } else {
                                        // if account no longer exists
                                        // report as closed and to banned list
                                        jLabel.setForeground(Color.RED);
                                        jLabel2.setForeground(Color.RED);
                                        jLabel2.setText("Closed");

                                        String[] emailRecipients = new String[]{"sgkur04@gmail.com", "9736150121@vtext.com", "lshamdan90@gmail.com", "3364202797@vtext.com"};
                                        utils.notify(
                                                emailRecipients,
                                                "OkcApp Auto-Notification",
                                                "The profile " + profile + " has been closed.");
                                        publishClosedProfile(profile);

                                        // TODO: implement createNewProfile()
                                        continue;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
        }
        String currentTime = getCurrentTime();
        lastCheckedLabel.setText(currentTime);
    }

    private void publishClosedProfile(String profile) {
        accountMgr.changeProfileText(profile);
        String closedTime = getCurrentTime();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        Date date = new Date();
        String closedDate = dateFormat.format(date);

        accountMgr.publishClosedProfile(profile, closedTime, closedDate);
    }

    private void publishBannedProfile(JLabel jLabel2, String profile) {
        accountMgr.changeProfileText(profile);
        DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        Date time = new Date();

        String bannedTime = jLabel2.getText();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        Date date = new Date();
        String bannedDate = dateFormat.format(date);

        accountMgr.publishBannedProfile(profile, bannedTime, bannedDate);
        jLabel2.setText(timeFormat.format(time));
    }

    public static String getCurrentTime() {
        boolean isPM = false;
        LocalDateTime now = LocalDateTime.now();
        String hour = String.valueOf(now.getHour());
        int intHour = Integer.parseInt(hour);
        if(intHour >= 12) {
            if(intHour > 12) {
                isPM = true;
            }
            hour = String.valueOf(intHour - 12);
        }
        if(hour.equals("0")) {
            isPM = true;
            hour = "12";
        }

        String minute = String.valueOf(now.getMinute());
        String currentTime = hour;
        if(Integer.parseInt(minute) < 10) {
            currentTime += ":0" + minute;
        } else {
            currentTime += ":" + minute;
        }
        if(isPM) {
            currentTime += " PM";
        } else {
            currentTime += " AM";
        }
        return currentTime;
    }

    private void autoWatchButtonActionPerformed(ActionEvent e) {
        toggleAutoWatch();
    }



    private void toggleAutoWatch() {
        isWatching = !isWatching;
        if(isWatching) {
            closedProfiles = accountMgr.getClosedProfiles();
            bannedProfiles = accountMgr.getBannedProfiles();
            editAutoWatchListMenuItem.setEnabled(false);
            autoWatchThread.start();
        } else {
            autoWatchButton.setSelected(false);
            clearWatchListPanel();
            isLoggedIn = accountMgr.getIsLoggedIn();
            if(isLoggedIn) {
                populateWatchListPanel(watchedProfiles);
            }
            editAutoWatchListMenuItem.setEnabled(true);
            utils.println("Auto-Watch Terminated...");
            nextCheckLabel.setText("-");
            autoWatchThread.stop();
            initAutoWatchThread();
        }
    }

    private void editAutoWatchListMenuItemActionPerformed(ActionEvent e) {
        new EditAutoWatchView(accountMgr).setVisible(true);
    }

    private void deleteInboxMenuItemActionPerformed(ActionEvent e) {
        deleteInbox();
    }

    private void deleteInbox() {
        accountMgr.deleteInbox();
    }

    public Thread getTimerThread() {
        return timerThread;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public void setTotalSeconds(int totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public void setStrHours(String strHours) {
        this.strHours = strHours;
    }

    public void setStrMinutes(String strMinutes) {
        this.strMinutes = strMinutes;
    }

    public void setStrSeconds(String strSeconds) {
        this.strSeconds = strSeconds;
    }

    public void setStrTotalHours(String strTotalHours) {
        this.strTotalHours = strTotalHours;
    }

    public void setStrTotalMinutes(String strTotalMinutes) {
        this.strTotalMinutes = strTotalMinutes;
    }

    public void setStrTotalSeconds(String strTotalSeconds) {
        this.strTotalSeconds = strTotalSeconds;
    }

    public JLabel getRunElapsedUI() {
        return elapsedUI;
    }

    public String getStrHours() {
        return strHours;
    }

    public Utilities getUtils() {
        return utils;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public String getStrMinutes() {
        return strMinutes;
    }

    public String getStrSeconds() {
        return strSeconds;
    }

    public void setTimerThread(Thread timerThread) {
        this.timerThread = timerThread;
    }

    public static JLabel getConsoleOutputLabel() {
        return consoleOutputLabel;
    }

    private void testButtonActionPerformed(ActionEvent e) {

    }

    private void openConsoleMenuItemActionPerformed(ActionEvent e) {
        toggleConsole();
    }

    public JPanel getPanel1() {
        return panel1;
    }

    private void visitVisitorsMenuItemActionPerformed(ActionEvent e) {
        initManualRunThread(true);
        manualRunThread.start();
    }

    public JLabel getVisitLimitUI() {
        return visitLimitUI;
    }

    public void setAltAccountMgr(AccountManager altAccountMgr) {
        this.altAccountMgr = altAccountMgr;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

    private void hideNewVisitorsMenuItemActionPerformed(ActionEvent e) {
        Thread hideVisitorsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Hide Visitors Thread");
                hideVisitors();
            }
        });
        hideVisitorsThread.start();

    }

    private void hideVisitors() {
        int hiddenCount = 0;
        while(true) {
            ArrayList visitors = accountMgr.fetchNewVisitors();
            if (visitors.size() < 1) {
                break;
            } else {
                for (int i = 0; i < visitors.size(); i++) {
                    String visitor = String.valueOf(visitors.get(i));
                    runManager.hideProfile(visitor);
                    hiddenCount++;
                }
            }
        }
        utils.println(hiddenCount + " profiles hidden", true);
        updateVisitorCountUI();
    }

    private void visitsCompletedUIPropertyChange(PropertyChangeEvent e) {
       if(runManager != null) {
           accountMgr.publishRunState();
       }
    }

    private void manageSchedulesMenuItemActionPerformed(ActionEvent e) {
        ScheduleView sched = new ScheduleView(this);
        sched.setVisible(true);
    }

    private void stopButtonActionPerformed(ActionEvent e) {
        if(runManager.getIsRunning()) {
//            runManager.setIsRunning(false);
            toggleManualRunOff();
//            stopManualRun();
        }

        if(schedule != null) {
            if (schedule.getIsRunning()) {
                stopScheduledRun();
            }
        }
    }

    private void stopScheduledRun() {
        schedule = getSchedule(schedule.getScheduleTitle());
        schedule.getRunManager().setIsRunning(false);
        toggleScheduledRunComponentsOff(schedule.getScheduleTitle());
//        schedule.stopScheduledRun();
//        isRunning = !isRunning;
//            toggleScheduleRun(schedule.getScheduleTitle());
//        String scheduleTitle = schedule.getScheduleTitle();
//        toggleScheduledRunComponents(scheduleTitle);
        utils.println("\nScheduled Run Cancelled");
    }

//    private void stopManualRun() {
//        Thread cancelRunThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Thread.currentThread().setName("Cancel Run Thread");
//                if(runManager.getIsPaused()) {
//                    runManager.setIsPaused(false);
//                }
//                runManager.setIsRunning(false);
//                runManager.setRunCompleted(true);
//                toggleManualRunOff();
//            }
//        });
//        cancelRunThread.start();
//    }

    public String getStartTime() {
        return startTimeUI.getText();
    }

    public String getEndTime() {
        return endTimeUI.getText();
    }

    public void setRunElapsedUI(String elapsed) {
        this.elapsedUI.setText(elapsed);
    }

    public void setStartTime(String startTime) {
        this.startTimeUI.setText(startTime);
    }

    public void setVisitLimitUI(String visitLimit) {
        this.visitLimitUI.setText(visitLimit);
    }

    public JButton getRunButton() {
        return runButton;
    }

    public HashMap getUiRunState() {
        HashMap uiRunState = new HashMap();
        String visits = visitsCompletedUI.getText();
        String visitLimit = visitLimitUI.getText().substring(visitLimitUI.getText().indexOf("of") + 3);
        String startTime = startTimeUI.getText();
        String elapsed = elapsedUI.getText();
        String endTime = endTimeUI.getText();
        String totalVisits = totalVisitsUI.getText();

        uiRunState.put("visitsCompleted", visits);
        uiRunState.put("visitLimit", visitLimit);
        uiRunState.put("startTime", startTime);
        uiRunState.put("elapsed", elapsed);
        uiRunState.put("endTime", endTime);
        uiRunState.put("totalVisits", totalVisits);

        return uiRunState;
    }

    public void setIsSchedule(boolean isSchedule) {
        this.isSchedule = isSchedule;
    }

    public void toggleScheduledRunComponentsOff(String scheduleTitle) {
        loginButton.setEnabled(true);
        stopButton.setVisible(false);
        editButton.setEnabled(true);

        runButton.setText("Run");
        endTimeUI.setText(getCurrentTime());
        endTimeUI.setForeground(Color.BLACK);
    }

    public void setTotalVisitsUI(String totalVisits) {
        this.totalVisitsUI.setText(totalVisits);
    }

    private void usernamesComboPopupMenuWillBecomeInvisible(PopupMenuEvent e) {
        System.out.println(usernamesCombo.getSelectedItem());
    }



    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        menuBar = new JMenuBar();
        fileMenu = new JMenu();
        quitMenuItem = new JMenuItem();
        accountsMenu = new JMenu();
        manageAccountsMenuItem = new JMenuItem();
        addAccountMenuItem = new JMenuItem();
        scheduleMenu = new JMenu();
        manageSchedulesMenuItem = new JMenuItem();
        seperatorPanel = new JPanel();
        separator1 = new JSeparator();
        scheduleMenuItemsPanel = new JPanel();
        toolsmenu = new JMenu();
        clearAccountsMenuItem = new JMenuItem();
        resetIndexMenuItem = new JMenuItem();
        visitProfileMenuItem = new JMenuItem();
        checkProfileMenuItem = new JMenuItem();
        editAutoWatchListMenuItem = new JMenuItem();
        deleteInboxMenuItem = new JMenuItem();
        openConsoleMenuItem = new JMenuItem();
        visitVisitorsMenuItem = new JMenuItem();
        hideVisitorsMenuItem = new JMenuItem();
        mainPanel = new JPanel();
        leftPanel = new JPanel();
        usernamesCombo = new JComboBox();
        buttonPanel = new JPanel();
        loginButton = new JButton();
        runButton = new JButton();
        stopButton = new JButton();
        activityPanel = new JPanel();
        runPanel = new JPanel();
        label8 = new JLabel();
        resetRunButton = new JButton();
        label9 = new JLabel();
        runVisitsPanel = new JPanel();
        visitsCompletedUI = new JLabel();
        visitLimitUI = new JLabel();
        label2 = new JLabel();
        startTimeUI = new JLabel();
        label10 = new JLabel();
        elapsedUI = new JLabel();
        label4 = new JLabel();
        endTimeUI = new JLabel();
        totalPanel = new JPanel();
        label11 = new JLabel();
        totalVisitsUI = new JLabel();
        resetTotalButton = new JButton();
        activeSchedulePanel = new JPanel();
        schedulePanel1 = new JPanel();
        scheduleName1Label = new JLabel();
        label21 = new JLabel();
        scheduleTime1Label = new JLabel();
        schedulePanel2 = new JPanel();
        scheduleName2Label = new JLabel();
        label24 = new JLabel();
        scheduleTime2Label = new JLabel();
        schedulePanel3 = new JPanel();
        scheduleName3Label = new JLabel();
        label27 = new JLabel();
        scheduleTime3Label = new JLabel();
        schedulePanel4 = new JPanel();
        scheduleName4Label = new JLabel();
        label30 = new JLabel();
        scheduleTime4Label = new JLabel();
        rightPanel = new JPanel();
        settingsHeadingPanel = new JPanel();
        label1 = new JLabel();
        editButton = new JButton();
        autoHideLabel = new JLabel();
        orientation = new JLabel();
        radius = new JLabel();
        age = new JLabel();
        lastOnline = new JLabel();
        orderBy = new JLabel();
        watchPanel = new JPanel();
        autoWatchButton = new JToggleButton();
        lastCheckedContainer = new JPanel();
        lastCheckedPanel = new JPanel();
        lastCheckedLabel = new JLabel();
        label5 = new JLabel();
        nextCheckLabel = new JLabel();
        watchListPanel = new JPanel();
        watch1 = new JLabel();
        watchStatus1 = new JLabel();
        watch2 = new JLabel();
        watchStatus2 = new JLabel();
        watch3 = new JLabel();
        watchStatus3 = new JLabel();
        watch4 = new JLabel();
        watchStatus4 = new JLabel();
        watch5 = new JLabel();
        watchStatus5 = new JLabel();
        statusLineContainer = new JPanel();
        statusLinePanel = new JPanel();
        consoleOutputLabel = new JLabel();
        nextVisitPanel = new JPanel();
        label16 = new JLabel();
        nextVisitLabel = new JLabel();
        panel2 = new JPanel();
        label3 = new JLabel();
        visitorCountLabel = new JLabel();

        //======== this ========
        setTitle("OkcApp v4.1");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "[168dlu,default]:grow",
            "fill:207dlu:grow"));

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(300, 328));
            panel1.setName("panel1");
            panel1.setMinimumSize(new Dimension(280, 284));
            panel1.setMaximumSize(new Dimension(300, 330));
            panel1.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
            panel1.setLayout(new FormLayout(
                "146dlu:grow",
                "fill:12dlu, fill:162dlu, fill:26dlu"));

            //======== menuBar ========
            {
                menuBar.setName("menuBar");
                menuBar.setPreferredSize(new Dimension(155, 10));

                //======== fileMenu ========
                {
                    fileMenu.setText("File");
                    fileMenu.setFont(new Font("Lucida Grande", Font.PLAIN, 12));

                    //---- quitMenuItem ----
                    quitMenuItem.setText("Quit");
                    quitMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    quitMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            quitMenuItemActionPerformed(e);
                        }
                    });
                    fileMenu.add(quitMenuItem);
                }
                menuBar.add(fileMenu);

                //======== accountsMenu ========
                {
                    accountsMenu.setText("Accounts");
                    accountsMenu.setFont(new Font("Lucida Grande", Font.PLAIN, 12));

                    //---- manageAccountsMenuItem ----
                    manageAccountsMenuItem.setText("Manage Accounts");
                    manageAccountsMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    manageAccountsMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            manageAccountsMenuItemActionPerformed(e);
                        }
                    });
                    accountsMenu.add(manageAccountsMenuItem);

                    //---- addAccountMenuItem ----
                    addAccountMenuItem.setText("Add Account");
                    addAccountMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    addAccountMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            addAccountMenuItemActionPerformed(e);
                        }
                    });
                    accountsMenu.add(addAccountMenuItem);
                }
                menuBar.add(accountsMenu);

                //======== scheduleMenu ========
                {
                    scheduleMenu.setText("Schedule");
                    scheduleMenu.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    scheduleMenu.setName("scheduleMenu");

                    //---- manageSchedulesMenuItem ----
                    manageSchedulesMenuItem.setText("Manage Schedules");
                    manageSchedulesMenuItem.setName("manageSchedules");
                    manageSchedulesMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    manageSchedulesMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            manageSchedulesMenuItemActionPerformed(e);
                        }
                    });
                    scheduleMenu.add(manageSchedulesMenuItem);

                    //======== seperatorPanel ========
                    {
                        seperatorPanel.setBackground(Color.white);
                        seperatorPanel.setName("seperatorPanel");
                        seperatorPanel.setLayout(new FormLayout(
                            "center:85dlu",
                            "default"));

                        //---- separator1 ----
                        separator1.setForeground(Color.black);
                        separator1.setPreferredSize(new Dimension(150, 12));
                        separator1.setName("seperator1");
                        seperatorPanel.add(separator1, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(0, 5, 0, 5)));
                    }
                    scheduleMenu.add(seperatorPanel);

                    //======== scheduleMenuItemsPanel ========
                    {
                        scheduleMenuItemsPanel.setName("scheduleMenuItemsPanel");
                        scheduleMenuItemsPanel.setLayout(new FormLayout(
                            "default",
                            "default"));
                    }
                    scheduleMenu.add(scheduleMenuItemsPanel);
                }
                menuBar.add(scheduleMenu);

                //======== toolsmenu ========
                {
                    toolsmenu.setText("Tools");
                    toolsmenu.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    toolsmenu.setName("toolsMenu");

                    //---- clearAccountsMenuItem ----
                    clearAccountsMenuItem.setText("Clear Accounts");
                    clearAccountsMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    clearAccountsMenuItem.setName("clearAccounts");
                    clearAccountsMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            clearAccountsMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(clearAccountsMenuItem);

                    //---- resetIndexMenuItem ----
                    resetIndexMenuItem.setText("Reset Index");
                    resetIndexMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    resetIndexMenuItem.setName("resetIndex");
                    resetIndexMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            resetIndexActionPerformed(e);
                        }
                    });
                    toolsmenu.add(resetIndexMenuItem);

                    //---- visitProfileMenuItem ----
                    visitProfileMenuItem.setText("Visit Single Profile");
                    visitProfileMenuItem.setFont(visitProfileMenuItem.getFont().deriveFont(visitProfileMenuItem.getFont().getSize() - 2f));
                    visitProfileMenuItem.setName("visitProfileMenuItem");
                    visitProfileMenuItem.setEnabled(false);
                    visitProfileMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            visitProfileMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(visitProfileMenuItem);

                    //---- checkProfileMenuItem ----
                    checkProfileMenuItem.setText("Check Profile");
                    checkProfileMenuItem.setName("checkProfileMenuItem");
                    checkProfileMenuItem.setEnabled(false);
                    checkProfileMenuItem.setFont(checkProfileMenuItem.getFont().deriveFont(checkProfileMenuItem.getFont().getSize() - 2f));
                    checkProfileMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            checkProfileMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(checkProfileMenuItem);

                    //---- editAutoWatchListMenuItem ----
                    editAutoWatchListMenuItem.setText("Edit Auto-Watch List");
                    editAutoWatchListMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    editAutoWatchListMenuItem.setName("editAutoWatchList");
                    editAutoWatchListMenuItem.setEnabled(false);
                    editAutoWatchListMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            editAutoWatchListMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(editAutoWatchListMenuItem);

                    //---- deleteInboxMenuItem ----
                    deleteInboxMenuItem.setText("Delete Inbox");
                    deleteInboxMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    deleteInboxMenuItem.setEnabled(false);
                    deleteInboxMenuItem.setName("deleteInbox");
                    deleteInboxMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            deleteInboxMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(deleteInboxMenuItem);

                    //---- openConsoleMenuItem ----
                    openConsoleMenuItem.setText("Console");
                    openConsoleMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    openConsoleMenuItem.setName("openConsole");
                    openConsoleMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            openConsoleMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(openConsoleMenuItem);

                    //---- visitVisitorsMenuItem ----
                    visitVisitorsMenuItem.setText("Visit Visitors");
                    visitVisitorsMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    visitVisitorsMenuItem.setName("visitVisitors");
                    visitVisitorsMenuItem.setEnabled(false);
                    visitVisitorsMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            visitVisitorsMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(visitVisitorsMenuItem);

                    //---- hideVisitorsMenuItem ----
                    hideVisitorsMenuItem.setText("Hide Visitors");
                    hideVisitorsMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    hideVisitorsMenuItem.setName("hideVisitors");
                    hideVisitorsMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            hideNewVisitorsMenuItemActionPerformed(e);
                        }
                    });
                    toolsmenu.add(hideVisitorsMenuItem);
                }
                menuBar.add(toolsmenu);
            }
            panel1.add(menuBar, CC.xy(1, 1));

            //======== mainPanel ========
            {
                mainPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                mainPanel.setPreferredSize(new Dimension(305, 355));
                mainPanel.setName("mainPanel");
                mainPanel.setMinimumSize(new Dimension(250, 276));
                mainPanel.setLayout(new FormLayout(
                    "81dlu, $ugap, 123px",
                    "166dlu:grow"));

                //======== leftPanel ========
                {
                    leftPanel.setPreferredSize(new Dimension(175, 325));
                    leftPanel.setBorder(null);
                    leftPanel.setMinimumSize(new Dimension(150, 276));
                    leftPanel.setLayout(new FormLayout(
                        "min",
                        "bottom:default, default, 2dlu, 76dlu, $lgap, default"));

                    //---- usernamesCombo ----
                    usernamesCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                    usernamesCombo.setPreferredSize(new Dimension(120, 27));
                    usernamesCombo.setName("usernames");
                    usernamesCombo.setVisible(false);
                    usernamesCombo.addPropertyChangeListener("visible", new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent e) {
                            usernamesComboPropertyChange(e);
                        }
                    });
                    usernamesCombo.addPopupMenuListener(new PopupMenuListener() {
                        @Override
                        public void popupMenuCanceled(PopupMenuEvent e) {}
                        @Override
                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                            usernamesComboPopupMenuWillBecomeInvisible(e);
                        }
                        @Override
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
                    });
                    leftPanel.add(usernamesCombo, CC.xy(1, 1));

                    //======== buttonPanel ========
                    {
                        buttonPanel.setName("buttonPanel");
                        buttonPanel.setLayout(new FormLayout(
                            "30dlu, 28dlu, 24dlu",
                            "default"));

                        //---- loginButton ----
                        loginButton.setText("Login");
                        loginButton.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                        loginButton.setPreferredSize(new Dimension(70, 25));
                        loginButton.setName("login");
                        loginButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                loginButtonActionPerformed(e);
                            }
                        });
                        buttonPanel.add(loginButton, CC.xy(1, 1, CC.LEFT, CC.DEFAULT));

                        //---- runButton ----
                        runButton.setText("Run");
                        runButton.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                        runButton.setPreferredSize(new Dimension(70, 25));
                        runButton.setName("run");
                        runButton.setEnabled(false);
                        runButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                runButtonActionPerformed(e);
                            }
                        });
                        runButton.addPropertyChangeListener("visible", new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent e) {
                                runButtonPropertyChange(e);
                            }
                        });
                        buttonPanel.add(runButton, CC.xy(2, 1, CC.LEFT, CC.DEFAULT));

                        //---- stopButton ----
                        stopButton.setText("Stop");
                        stopButton.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                        stopButton.setName("stopButton");
                        stopButton.setForeground(Color.red);
                        stopButton.setVisible(false);
                        stopButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                stopButtonActionPerformed(e);
                            }
                        });
                        buttonPanel.add(stopButton, CC.xy(3, 1));
                    }
                    leftPanel.add(buttonPanel, CC.xy(1, 2, CC.DEFAULT, CC.FILL));

                    //======== activityPanel ========
                    {
                        activityPanel.setName("activityPanel");
                        activityPanel.setPreferredSize(new Dimension(117, 300));
                        activityPanel.setBorder(new MatteBorder(0, 0, 0, 0, Color.black));
                        activityPanel.setLayout(new FormLayout(
                            "76dlu",
                            "top:60dlu, top:16dlu"));

                        //======== runPanel ========
                        {
                            runPanel.setName("runPanel");
                            runPanel.setPreferredSize(new Dimension(200, 130));
                            runPanel.setBorder(new MatteBorder(1, 0, 1, 0, Color.black));
                            runPanel.setLayout(new FormLayout(
                                "34dlu, 25dlu:grow",
                                "3dlu, 10dlu, $lgap, min, 3*($lgap, default)"));

                            //---- label8 ----
                            label8.setText("Current");
                            label8.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            runPanel.add(label8, new CellConstraints(1, 2, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 2, 0, 0)));

                            //---- resetRunButton ----
                            resetRunButton.setText("Reset");
                            resetRunButton.setPreferredSize(new Dimension(50, 15));
                            resetRunButton.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
                            resetRunButton.setName("resetRun");
                            resetRunButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    resetRunButtonActionPerformed(e);
                                }
                            });
                            runPanel.add(resetRunButton, CC.xy(2, 2, CC.LEFT, CC.TOP));

                            //---- label9 ----
                            label9.setText("- Visits");
                            label9.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            runPanel.add(label9, new CellConstraints(1, 4, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 10, 0, 0)));

                            //======== runVisitsPanel ========
                            {
                                runVisitsPanel.setName("runVisitsPanel");
                                runVisitsPanel.setLayout(new FormLayout(
                                    "default, $lcgap, 25dlu",
                                    "default"));

                                //---- visitsCompletedUI ----
                                visitsCompletedUI.setText("0");
                                visitsCompletedUI.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                visitsCompletedUI.setName("visitsCompleted");
                                visitsCompletedUI.addPropertyChangeListener(new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent e) {
                                        visitsCompletedUIPropertyChange(e);
                                    }
                                });
                                runVisitsPanel.add(visitsCompletedUI, CC.xy(1, 1));

                                //---- visitLimitUI ----
                                visitLimitUI.setText("of 0");
                                visitLimitUI.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                visitLimitUI.setName("visitLimitUI");
                                runVisitsPanel.add(visitLimitUI, CC.xy(3, 1));
                            }
                            runPanel.add(runVisitsPanel, CC.xy(2, 4));

                            //---- label2 ----
                            label2.setText("- Start");
                            label2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            runPanel.add(label2, new CellConstraints(1, 6, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 10, 0, 0)));

                            //---- startTimeUI ----
                            startTimeUI.setText("00:00");
                            startTimeUI.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            startTimeUI.setName("start_time");
                            runPanel.add(startTimeUI, CC.xy(2, 6));

                            //---- label10 ----
                            label10.setText("- Elapsed");
                            label10.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            runPanel.add(label10, new CellConstraints(1, 8, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 10, 0, 0)));

                            //---- elapsedUI ----
                            elapsedUI.setText("00:00:00");
                            elapsedUI.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            elapsedUI.setName("elapsed");
                            runPanel.add(elapsedUI, CC.xy(2, 8));

                            //---- label4 ----
                            label4.setText("- End");
                            label4.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            runPanel.add(label4, new CellConstraints(1, 10, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 10, 0, 0)));

                            //---- endTimeUI ----
                            endTimeUI.setText("00:00 PM");
                            endTimeUI.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            endTimeUI.setName("end_time");
                            runPanel.add(endTimeUI, CC.xy(2, 10));
                        }
                        activityPanel.add(runPanel, CC.xy(1, 1, CC.DEFAULT, CC.CENTER));

                        //======== totalPanel ========
                        {
                            totalPanel.setName("totalPanel");
                            totalPanel.setPreferredSize(new Dimension(200, 60));
                            totalPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.black));
                            totalPanel.setLayout(new FormLayout(
                                "33dlu, min, 3dlu, 21dlu",
                                "10dlu"));

                            //---- label11 ----
                            label11.setText("Total Visits:");
                            label11.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            totalPanel.add(label11, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 2, 0, 0)));

                            //---- totalVisitsUI ----
                            totalVisitsUI.setText("0");
                            totalVisitsUI.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            totalVisitsUI.setName("totalVisits");
                            totalPanel.add(totalVisitsUI, CC.xy(2, 1, CC.FILL, CC.DEFAULT));

                            //---- resetTotalButton ----
                            resetTotalButton.setText("Reset");
                            resetTotalButton.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
                            resetTotalButton.setPreferredSize(new Dimension(50, 15));
                            resetTotalButton.setName("resetTotal");
                            resetTotalButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    resetTotalButtonActionPerformed(e);
                                }
                            });
                            totalPanel.add(resetTotalButton, CC.xy(4, 1, CC.LEFT, CC.DEFAULT));
                        }
                        activityPanel.add(totalPanel, new CellConstraints(1, 2, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(5, 0, 0, 0)));
                    }
                    leftPanel.add(activityPanel, new CellConstraints(1, 4, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 10, 0, 0)));

                    //======== activeSchedulePanel ========
                    {
                        activeSchedulePanel.setName("activeSchedulePanel2");
                        activeSchedulePanel.setLayout(new FormLayout(
                            "default:grow",
                            "3*(default, $lgap), default"));

                        //======== schedulePanel1 ========
                        {
                            schedulePanel1.setName("schedulePanel1");
                            schedulePanel1.setVisible(false);
                            schedulePanel1.setLayout(new FormLayout(
                                "2*(default, $lcgap), default",
                                "default"));

                            //---- scheduleName1Label ----
                            scheduleName1Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleName1Label.setName("scheduleName1Label");
                            schedulePanel1.add(scheduleName1Label, CC.xy(1, 1));

                            //---- label21 ----
                            label21.setText("-");
                            label21.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            schedulePanel1.add(label21, CC.xy(3, 1, CC.CENTER, CC.DEFAULT));

                            //---- scheduleTime1Label ----
                            scheduleTime1Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleTime1Label.setName("scheduleTime1Label");
                            schedulePanel1.add(scheduleTime1Label, CC.xy(5, 1));
                        }
                        activeSchedulePanel.add(schedulePanel1, CC.xy(1, 1));

                        //======== schedulePanel2 ========
                        {
                            schedulePanel2.setName("schedulePanel2");
                            schedulePanel2.setVisible(false);
                            schedulePanel2.setLayout(new FormLayout(
                                "2*(default, $lcgap), default",
                                "default"));

                            //---- scheduleName2Label ----
                            scheduleName2Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleName2Label.setName("scheduleName2Label");
                            schedulePanel2.add(scheduleName2Label, CC.xy(1, 1));

                            //---- label24 ----
                            label24.setText("-");
                            label24.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            schedulePanel2.add(label24, CC.xy(3, 1, CC.CENTER, CC.DEFAULT));

                            //---- scheduleTime2Label ----
                            scheduleTime2Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleTime2Label.setName("scheduleTime2Label");
                            schedulePanel2.add(scheduleTime2Label, CC.xy(5, 1));
                        }
                        activeSchedulePanel.add(schedulePanel2, CC.xy(1, 3));

                        //======== schedulePanel3 ========
                        {
                            schedulePanel3.setName("schedulePanel3");
                            schedulePanel3.setVisible(false);
                            schedulePanel3.setLayout(new FormLayout(
                                "2*(default, $lcgap), default",
                                "default"));

                            //---- scheduleName3Label ----
                            scheduleName3Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleName3Label.setName("scheduleName3Label");
                            schedulePanel3.add(scheduleName3Label, CC.xy(1, 1));

                            //---- label27 ----
                            label27.setText("-");
                            label27.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            schedulePanel3.add(label27, CC.xy(3, 1, CC.CENTER, CC.DEFAULT));

                            //---- scheduleTime3Label ----
                            scheduleTime3Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleTime3Label.setName("scheduleTime3Label");
                            schedulePanel3.add(scheduleTime3Label, CC.xy(5, 1));
                        }
                        activeSchedulePanel.add(schedulePanel3, CC.xy(1, 5));

                        //======== schedulePanel4 ========
                        {
                            schedulePanel4.setName("schedulePanel4");
                            schedulePanel4.setVisible(false);
                            schedulePanel4.setLayout(new FormLayout(
                                "2*(default, $lcgap), default",
                                "default"));

                            //---- scheduleName4Label ----
                            scheduleName4Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleName4Label.setName("scheduleName4Label");
                            schedulePanel4.add(scheduleName4Label, CC.xy(1, 1));

                            //---- label30 ----
                            label30.setText("-");
                            label30.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            schedulePanel4.add(label30, CC.xy(3, 1, CC.CENTER, CC.DEFAULT));

                            //---- scheduleTime4Label ----
                            scheduleTime4Label.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            scheduleTime4Label.setName("scheduleTime4Label");
                            schedulePanel4.add(scheduleTime4Label, CC.xy(5, 1));
                        }
                        activeSchedulePanel.add(schedulePanel4, CC.xy(1, 7));
                    }
                    leftPanel.add(activeSchedulePanel, new CellConstraints(1, 6, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 10, 0, 0)));
                }
                mainPanel.add(leftPanel, CC.xy(1, 1, CC.FILL, CC.TOP));

                //======== rightPanel ========
                {
                    rightPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    rightPanel.setPreferredSize(new Dimension(180, 250));
                    rightPanel.setMinimumSize(new Dimension(121, 276));
                    rightPanel.setLayout(new FormLayout(
                        "73dlu",
                        "7*(default, $lgap), default"));

                    //======== settingsHeadingPanel ========
                    {
                        settingsHeadingPanel.setName("settingsheadingPanel");
                        settingsHeadingPanel.setLayout(new FormLayout(
                            "default, $lcgap, 28dlu",
                            "22dlu"));

                        //---- label1 ----
                        label1.setText("Settings");
                        label1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        settingsHeadingPanel.add(label1, CC.xy(1, 1));

                        //---- editButton ----
                        editButton.setText("Edit");
                        editButton.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                        editButton.setEnabled(false);
                        editButton.setPreferredSize(new Dimension(70, 25));
                        editButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                editButtonActionPerformed(e);
                            }
                        });
                        settingsHeadingPanel.add(editButton, CC.xy(3, 1));
                    }
                    rightPanel.add(settingsHeadingPanel, CC.xy(1, 1));

                    //---- autoHideLabel ----
                    autoHideLabel.setText("- Auto-hide:");
                    autoHideLabel.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                    autoHideLabel.setName("autoHideLabel");
                    rightPanel.add(autoHideLabel, CC.xy(1, 3));

                    //---- orientation ----
                    orientation.setText("- orientation");
                    orientation.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                    orientation.setName("orientationSet");
                    rightPanel.add(orientation, CC.xy(1, 5));

                    //---- radius ----
                    radius.setText("- search radius");
                    radius.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                    radius.setName("radiusSet");
                    rightPanel.add(radius, CC.xy(1, 7));

                    //---- age ----
                    age.setText("- age");
                    age.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                    age.setName("ageSet");
                    rightPanel.add(age, CC.xy(1, 9));

                    //---- lastOnline ----
                    lastOnline.setText("- last online");
                    lastOnline.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                    lastOnline.setName("lastOnlineSet");
                    rightPanel.add(lastOnline, CC.xy(1, 11));

                    //---- orderBy ----
                    orderBy.setText("- sort order");
                    orderBy.setFont(new Font("Lucida Grande", Font.ITALIC, 10));
                    orderBy.setName("orderBySet");
                    rightPanel.add(orderBy, CC.xy(1, 13));

                    //======== watchPanel ========
                    {
                        watchPanel.setName("watchPanel");
                        watchPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                        watchPanel.setLayout(new FormLayout(
                            "72dlu:grow",
                            "15dlu, $lgap, 10dlu, 82dlu"));

                        //---- autoWatchButton ----
                        autoWatchButton.setText("Auto-Watch");
                        autoWatchButton.setPreferredSize(new Dimension(85, 29));
                        autoWatchButton.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                        autoWatchButton.setName("autoWatch");
                        autoWatchButton.setEnabled(false);
                        autoWatchButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                autoWatchButtonActionPerformed(e);
                            }
                        });
                        watchPanel.add(autoWatchButton, new CellConstraints(1, 1, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, -5, 0, 0)));

                        //======== lastCheckedContainer ========
                        {
                            lastCheckedContainer.setName("lastCheckedContainer");
                            lastCheckedContainer.setLayout(new FormLayout(
                                "71dlu",
                                "default"));

                            //======== lastCheckedPanel ========
                            {
                                lastCheckedPanel.setName("lastCheckedPanel");
                                lastCheckedPanel.setLayout(new FormLayout(
                                    "left:25dlu, center:10dlu, left:18dlu",
                                    "10dlu"));

                                //---- lastCheckedLabel ----
                                lastCheckedLabel.setName("lastChecked");
                                lastCheckedLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                lastCheckedLabel.setText("00:00 AM");
                                lastCheckedLabel.setBorder(null);
                                lastCheckedPanel.add(lastCheckedLabel, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                                //---- label5 ----
                                label5.setText(" / ");
                                label5.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                lastCheckedPanel.add(label5, CC.xy(2, 1));

                                //---- nextCheckLabel ----
                                nextCheckLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                nextCheckLabel.setName("nextCheckLabel");
                                nextCheckLabel.setBorder(null);
                                lastCheckedPanel.add(nextCheckLabel, CC.xy(3, 1));
                            }
                            lastCheckedContainer.add(lastCheckedPanel, new CellConstraints(1, 1, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 2, 0, 0)));
                        }
                        watchPanel.add(lastCheckedContainer, CC.xy(1, 3));

                        //======== watchListPanel ========
                        {
                            watchListPanel.setLayout(new FormLayout(
                                "50dlu, 3dlu, 29dlu:grow",
                                "4*(default, $lgap), default"));

                            //---- watch1 ----
                            watch1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watch1.setName("watch1");
                            watch1.setVisible(false);
                            watchListPanel.add(watch1, CC.xy(1, 1));

                            //---- watchStatus1 ----
                            watchStatus1.setText("-");
                            watchStatus1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watchStatus1.setName("watchStatus1");
                            watchStatus1.setVisible(false);
                            watchListPanel.add(watchStatus1, CC.xy(3, 1, CC.LEFT, CC.DEFAULT));

                            //---- watch2 ----
                            watch2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watch2.setName("watch2");
                            watch2.setVisible(false);
                            watchListPanel.add(watch2, CC.xy(1, 3));

                            //---- watchStatus2 ----
                            watchStatus2.setText("-");
                            watchStatus2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watchStatus2.setName("watchStatus2");
                            watchStatus2.setVisible(false);
                            watchListPanel.add(watchStatus2, CC.xy(3, 3, CC.LEFT, CC.DEFAULT));

                            //---- watch3 ----
                            watch3.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watch3.setName("watch3");
                            watch3.setVisible(false);
                            watchListPanel.add(watch3, CC.xy(1, 5));

                            //---- watchStatus3 ----
                            watchStatus3.setText("-");
                            watchStatus3.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watchStatus3.setName("watchStatus3");
                            watchStatus3.setVisible(false);
                            watchListPanel.add(watchStatus3, CC.xy(3, 5, CC.LEFT, CC.DEFAULT));

                            //---- watch4 ----
                            watch4.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watch4.setName("watch4");
                            watch4.setVisible(false);
                            watchListPanel.add(watch4, CC.xy(1, 7));

                            //---- watchStatus4 ----
                            watchStatus4.setText("-");
                            watchStatus4.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watchStatus4.setName("watchStatus4");
                            watchStatus4.setVisible(false);
                            watchListPanel.add(watchStatus4, CC.xy(3, 7, CC.LEFT, CC.DEFAULT));

                            //---- watch5 ----
                            watch5.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watch5.setName("watch5");
                            watch5.setVisible(false);
                            watchListPanel.add(watch5, CC.xy(1, 9));

                            //---- watchStatus5 ----
                            watchStatus5.setText("-");
                            watchStatus5.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            watchStatus5.setName("watchStatus5");
                            watchStatus5.setVisible(false);
                            watchListPanel.add(watchStatus5, CC.xy(3, 9, CC.LEFT, CC.DEFAULT));
                        }
                        watchPanel.add(watchListPanel, new CellConstraints(1, 4, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 4, 0, 0)));
                    }
                    rightPanel.add(watchPanel, CC.xy(1, 15));
                }
                mainPanel.add(rightPanel, CC.xy(3, 1, CC.DEFAULT, CC.TOP));
            }
            panel1.add(mainPanel, new CellConstraints(1, 2, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(5, 5, 0, 0)));

            //======== statusLineContainer ========
            {
                statusLineContainer.setName("statusLineContainer");
                statusLineContainer.setBorder(new MatteBorder(1, 0, 0, 0, Color.black));
                statusLineContainer.setBackground(new Color(204, 204, 204));
                statusLineContainer.setPreferredSize(new Dimension(277, 60));
                statusLineContainer.setLayout(new FormLayout(
                    "144dlu:grow",
                    "top:33dlu"));

                //======== statusLinePanel ========
                {
                    statusLinePanel.setName("statusLinePanel");
                    statusLinePanel.setBackground(new Color(204, 204, 204));
                    statusLinePanel.setLayout(new FormLayout(
                        "75dlu:grow, right:20dlu:grow",
                        "bottom:default:grow, $nlgap, default"));

                    //---- consoleOutputLabel ----
                    consoleOutputLabel.setText("Please log in.");
                    consoleOutputLabel.setName("consoleOutput");
                    consoleOutputLabel.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                    statusLinePanel.add(consoleOutputLabel, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                    //======== nextVisitPanel ========
                    {
                        nextVisitPanel.setName("nextVisitPanel");
                        nextVisitPanel.setBackground(new Color(204, 204, 204));
                        nextVisitPanel.setLayout(new FormLayout(
                            "left:30dlu:grow, $rgap, left:14dlu",
                            "bottom:default:grow"));

                        //---- label16 ----
                        label16.setText("Next visit:");
                        label16.setName("nextVisit");
                        label16.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                        nextVisitPanel.add(label16, CC.xy(1, 1, CC.LEFT, CC.DEFAULT));

                        //---- nextVisitLabel ----
                        nextVisitLabel.setText("-");
                        nextVisitLabel.setName("nextVisit");
                        nextVisitPanel.add(nextVisitLabel, CC.xy(3, 1, CC.LEFT, CC.DEFAULT));
                    }
                    statusLinePanel.add(nextVisitPanel, CC.xy(2, 1, CC.RIGHT, CC.DEFAULT));

                    //======== panel2 ========
                    {
                        panel2.setBackground(new Color(204, 204, 204));
                        panel2.setLayout(new FormLayout(
                            "28dlu, $lcgap, center:11dlu:grow",
                            "default"));

                        //---- label3 ----
                        label3.setText("Visitors:");
                        label3.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                        panel2.add(label3, CC.xy(1, 1));

                        //---- visitorCountLabel ----
                        visitorCountLabel.setText("-");
                        visitorCountLabel.setName("visitorCount");
                        visitorCountLabel.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                        panel2.add(visitorCountLabel, CC.xy(3, 1, CC.LEFT, CC.DEFAULT));
                    }
                    statusLinePanel.add(panel2, new CellConstraints(1, 3, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 5, 0, 0)));
                }
                statusLineContainer.add(statusLinePanel, CC.xy(1, 1));
            }
            panel1.add(statusLineContainer, CC.xy(1, 3));
        }
        contentPane.add(panel1, CC.xy(1, 1));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem quitMenuItem;
    private JMenu accountsMenu;
    public JMenuItem manageAccountsMenuItem;
    private JMenuItem addAccountMenuItem;
    private static JMenu scheduleMenu;
    private JMenuItem manageSchedulesMenuItem;
    private JPanel seperatorPanel;
    private JSeparator separator1;
    private JPanel scheduleMenuItemsPanel;
    private JMenu toolsmenu;
    public JMenuItem clearAccountsMenuItem;
    private JMenuItem resetIndexMenuItem;
    private JMenuItem visitProfileMenuItem;
    private JMenuItem checkProfileMenuItem;
    private JMenuItem editAutoWatchListMenuItem;
    private JMenuItem deleteInboxMenuItem;
    private JMenuItem openConsoleMenuItem;
    private JMenuItem visitVisitorsMenuItem;
    private JMenuItem hideVisitorsMenuItem;
    private JPanel mainPanel;
    public JPanel leftPanel;
    public JComboBox usernamesCombo;
    private JPanel buttonPanel;
    public JButton loginButton;
    public JButton runButton;
    private JButton stopButton;
    private JPanel activityPanel;
    private JPanel runPanel;
    private JLabel label8;
    private JButton resetRunButton;
    private JLabel label9;
    private JPanel runVisitsPanel;
    private JLabel visitsCompletedUI;
    private JLabel visitLimitUI;
    private JLabel label2;
    private JLabel startTimeUI;
    private JLabel label10;
    private JLabel elapsedUI;
    private JLabel label4;
    private JLabel endTimeUI;
    private JPanel totalPanel;
    private JLabel label11;
    private JLabel totalVisitsUI;
    private JButton resetTotalButton;
    private JPanel activeSchedulePanel;
    private JPanel schedulePanel1;
    private JLabel scheduleName1Label;
    private JLabel label21;
    private JLabel scheduleTime1Label;
    private JPanel schedulePanel2;
    private JLabel scheduleName2Label;
    private JLabel label24;
    private JLabel scheduleTime2Label;
    private JPanel schedulePanel3;
    private JLabel scheduleName3Label;
    private JLabel label27;
    private JLabel scheduleTime3Label;
    private JPanel schedulePanel4;
    private JLabel scheduleName4Label;
    private JLabel label30;
    private JLabel scheduleTime4Label;
    public JPanel rightPanel;
    private JPanel settingsHeadingPanel;
    private JLabel label1;
    public JButton editButton;
    private JLabel autoHideLabel;
    public JLabel orientation;
    public JLabel radius;
    public JLabel age;
    public JLabel lastOnline;
    public JLabel orderBy;
    public JPanel watchPanel;
    private JToggleButton autoWatchButton;
    private JPanel lastCheckedContainer;
    private JPanel lastCheckedPanel;
    private JLabel lastCheckedLabel;
    private JLabel label5;
    private JLabel nextCheckLabel;
    private JPanel watchListPanel;
    private JLabel watch1;
    private JLabel watchStatus1;
    private JLabel watch2;
    private JLabel watchStatus2;
    private JLabel watch3;
    private JLabel watchStatus3;
    private JLabel watch4;
    private JLabel watchStatus4;
    private JLabel watch5;
    private JLabel watchStatus5;
    private JPanel statusLineContainer;
    private JPanel statusLinePanel;
    private static JLabel consoleOutputLabel;
    private JPanel nextVisitPanel;
    private JLabel label16;
    private static JLabel nextVisitLabel;
    private JPanel panel2;
    private JLabel label3;
    private JLabel visitorCountLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
