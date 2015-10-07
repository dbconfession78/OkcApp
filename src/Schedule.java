import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by stuartkuredjian on 9/24/15.
 */
public class Schedule {
    private String scheduleFolder;
    private Boolean isRunning = false;
    private Thread scheduleThread;
    private JMenuItem scheduleMenuItem = new JMenuItem();
    private MainView mainView;
    private AccountManager accountMgr;
    private final String scheduleTitle;
    private SearchPreferencesView searchPreferencesView;
    private HashMap searchSettingsMap = new HashMap<>();
    private static Preferences _prefs;
    private String startTime;
    private RunManager runManager;
    private boolean isActivated = false;
    private boolean isListening = false;
    private Utilities utils = new Utilities();

    public Schedule(final MainView mainView , final String schedTitle) {
        searchSettingsMap = ScheduleView.fetchSearchSettings(schedTitle);
        this.scheduleTitle = schedTitle;
        this.mainView = mainView;
        this.accountMgr = mainView.accountMgr;
        runManager = new RunManager(this, mainView.accountMgr);

        // create the menuItem and assign its properties and listeners
        scheduleFolder = Schedule.fetchSchedFolderBySchedTitle(schedTitle);
        scheduleMenuItem = new JMenuItem();
        scheduleMenuItem.setName(scheduleFolder);
        scheduleMenuItem.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        scheduleMenuItem.setText(schedTitle);
//        scheduleMenuItem.setFocusable(false);
        scheduleMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.scheduleMenuItemActionPerformed(e, schedTitle);
            }
        });
        initScheduleThread();
    }

    public static String fetchSchedFolderBySchedTitle(String schedTitle) {
        String schedFolder = "";
        String[] childrenNames = fetchChildrenNames("OkcAccounts");
        for (int i = 0; i < childrenNames.length; i++) {
            String childName = childrenNames[i];
            if(childName.startsWith("schedule")) {
                schedFolder = childName;
                _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
                String schedTitle2 = _prefs.get("scheduleTitle", "");
                if(schedTitle2.equals(schedTitle)) {
                    break;
                }
            }

        }
        return schedFolder;
    }

    private static String[] fetchChildrenNames(String path) {
        String[] childrenNames = {};
        _prefs = Preferences.userRoot().node(path);
        try {
            childrenNames = _prefs.childrenNames();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return childrenNames;
    }

    public void initScheduleThread() {
        final Schedule schedule = this;
        scheduleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // this is where scheduleThread.start() begins
                Thread.currentThread().setName("Schedule Thread");
                _prefs = Preferences.userRoot().node("OkcAccounts/" + scheduleFolder);
                startTime = String.valueOf(_prefs.get("startTime", ""));
//                startTime = MainView.getCurrentTime(); //TODO:  uncomment this line and comment the line above for testing.
                Thread.currentThread().setName("Schedule Thread");

                // begin listening
                while (isActivated) {
                    mainView.toggleScheduleMenuItemPropertiesOn(scheduleTitle);
                    String currentTime = MainView.getCurrentTime();
                    if (currentTime.equals(startTime)) {
                        // when scheduled time arrives
                        // wait if another run is already in progress
                        while(mainView.runManager.getIsRunning()) {
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // once there aren't any other runs in progress
                        // begin the scheduled run
                        mainView.resetRunMetrics();
                        mainView.toggleScheduledRunComponentsOn(scheduleTitle);
                        System.out.println("The current time is: " + currentTime);
                        runManager = new RunManager(schedule, mainView.accountMgr);
                        runManager.run();
                        mainView.toggleScheduledRunComponentsOff(scheduleTitle);
                    }
                }
            }
        });
    }

    public JMenuItem getScheduleMenuItem() {
        return this.scheduleMenuItem;
    }

    public Boolean getIsRunning() {
        return runManager.getIsRunning();
    }

    public String getScheduleTitle() {
        return this.scheduleTitle;
    }

    public HashMap<String, String> getSearchSettingsMap() {
        return searchSettingsMap;
    }

    public HashMap<String, String> fetchScheduleSettings(String scheduleTitle) {
        HashMap scheduleSettings = new HashMap();
        try {
            String scheduleFolder = fetchSchedFolderBySchedTitle(scheduleTitle);
            _prefs = Preferences.userRoot().node("OkcAccounts/" + scheduleFolder);
            String[] keys = _prefs.keys();

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if(key.equals("orderBy")) {
                    String value = _prefs.get(key, "");
                    scheduleSettings.put(key, value);
                } else {
                    String value = _prefs.get(key, "").toLowerCase();
                    scheduleSettings.put(key, value);
                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return scheduleSettings;
    }

    public RunManager getRunManager() {
        return runManager;
    }

    public void stopScheduledRun() {
//        mainView.toggleScheduledRunComponentsOff(scheduleTitle);
//        runManager.setIsRunning(false);
    }

    public boolean getIsActivated() {
        return isActivated;
    }

    public void setIsActivated(boolean isActivated) {
        this.isActivated = isActivated;
    }

    public void run() {
        initScheduleThread();
        scheduleThread.start();
    }
}
