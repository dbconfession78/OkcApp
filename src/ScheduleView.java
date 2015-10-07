import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Fri Sep 18 16:51:04 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class ScheduleView extends JFrame {
    private int scheduleCount;
    private JButton cancelButton;
    private JButton addButton;
    private JButton applyButton;
    private JTextField schedTitleTextField;
    private JLabel colonLabel;
    private JCheckBox freeRunCheckBox;;
    private static Preferences _prefs;
    private static HashMap searchSettingsMap = new HashMap();
    private SearchPreferencesView searchPreferencesView;
    private String schedTitle = "";
    private boolean isEditView = false;
    private Boolean prefsModified = false;
    private Utilities utils = new Utilities();
    private AbstractAction componentAction;
    private HashMap initialValues;
    private boolean hasChanges = false;
    private KeyListener keyReleased;
    private Container contentPane;
    private MainView mainView;
    private ArrayList<Schedule> schedules;

    public ScheduleView(MainView mainView) {
        this.mainView = mainView;
        initComponents();
        initSecondaryComponents();
        onLoad();
    }

    private void onLoad() {
        scheduleCount = fetchScheduleCount();
        searchPreferencesView = new SearchPreferencesView(this);

        // set initial view depending on schedule count
        if(scheduleCount > 0)  {
            setMainView();
        } else {
            setNewView();
        }
        this.setVisible(true);
    }

    private void initChangeListener() {
        componentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(initialValues != null) {
                    compareChanges(initialValues);
                }
            }
        };
        keyReleased = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                compareChanges(initialValues);
            }
        };
        addListenerToPanelComponents(panel1);
    }

    private void addListenerToPanelComponents(JPanel jPanel) {
        Component[] components = jPanel.getComponents();
        for (int i = 0; i < jPanel.getComponentCount(); i++) {
            Component component = components[i];
            String componentClassName = component.getClass().getName();

            if(componentClassName.equals("javax.swing.JPanel")) {
                JPanel jPanel2 = (JPanel) component;
                addListenerToPanelComponents(jPanel2);
            }

            if(componentClassName.equals("javax.swing.JButton")) {
                JButton jButton = (JButton) component;
                jButton.addActionListener(componentAction);
            }

            if(componentClassName.equals("javax.swing.JCheckBox")) {
                JCheckBox jCheckBox = (JCheckBox) component;
                jCheckBox.addActionListener(componentAction);
            }

            if(componentClassName.equals("javax.swing.JComboBox")) {
                JComboBox jComboBox= (JComboBox) component;
                jComboBox.addActionListener(componentAction);
            }

            if(componentClassName.equals("javax.swing.JTextField")) {
                JTextField jTextField= (JTextField) component;
                jTextField.addKeyListener(keyReleased);
            }
        }
    }

    private void compareChanges(final HashMap initialValues) {
        while(true) {
            hasChanges = false;
            HashMap<String, String> currentValues = new HashMap<>();
            currentValues = utils.generateUserInputMap(panel1,currentValues);
            utils.populateMap(panel1, currentValues);
            Iterator iterator = currentValues.keySet().iterator();
            while (iterator.hasNext()) {
                String key = String.valueOf(iterator.next());
                String currentValue = currentValues.get(key);
                String initialValue = String.valueOf(initialValues.get(key));

                if (!currentValue.equals(initialValue)) {
                    if (!currentValue.equals("")) {
                        hasChanges = true;
                        break;
                    }
                }
            }
            if (hasChanges) {
                if(schedTitleTextField.getText().equals("")) {
                    hasChanges = false;
                    applyButton.setEnabled(false);
                } else {
                    applyButton.setEnabled(true);
                }
            } else {
                applyButton.setEnabled(false);
            }
            break;
        }
    }

    public static HashMap fetchSearchSettings(String schedTitle) {
        String schedFolder = Schedule.fetchSchedFolderBySchedTitle(schedTitle);
        searchSettingsMap = new HashMap();
        _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
        try {
            String[] keys = _prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                String value = _prefs.get(key, "");
                searchSettingsMap.put(key, value);
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        return searchSettingsMap;
    }

    private void setNewView() {
        getContentPane().setLayout(new FormLayout(
                "139dlu",
                "129dlu"
        ));
        pack();
        schedTitleTextField.setVisible(true);
        schedTitleComboBox.setVisible(false);
        schedTitleTextField.setText("");

        startTimeInnerPanel2.setVisible(true);
        startTimeInnerPanel1.setVisible(false);
        startTimeHourTextField.setText("12");
        startTimeMinuteTextField.setText("00");

        freeRunCheckBox.setVisible(true);
        freeRunLabel.setVisible(false);

        searchPreferencesButton.setVisible(true);

        addButton.setVisible(true);
        editButton.setVisible(false);
        applyButton.setVisible(false);

        removeButton.setVisible(false);

        if(fetchScheduleCount() > 0) {
            cancelButton.setVisible(true);
            closeButton.setVisible(false);
        } else {
            cancelButton.setVisible(false);
            closeButton.setVisible(true);
        }

        newButton.setVisible(false);
    }

    private void initSecondaryComponents() {
        initCancelButton();
        initAddButton();
        initApplyButton();

        initSchedTitleTextField();
        initFreeRunCheckBox();

    }

    private void initFreeRunCheckBox() {
        freeRunCheckBox = new JCheckBox();
        freeRunCheckBox.setName("freeRunCheckBox");
        freeRunPanel.add(freeRunCheckBox, CC.xy(2, 1));
//        freeRunPanel.setVisible(false);
    }

    private void initSchedTitleTextField() {
        schedTitleTextField = new JTextField();
        schedTitleTextField.setName("schedTitleTextField");
        schedTitlePanel.add(schedTitleTextField, CC.xy(3, 1));
        schedTitleTextField.setVisible(false);
    }

    private void initApplyButton() {
        applyButton = new JButton();
        applyButton.setText("Apply");
        buttonPanel.add(applyButton, CC.xy(1, 1));
        applyButton.setVisible(false);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyButtonActionPerformed(e);
            }
        });
    }

    private void applyButtonActionPerformed(ActionEvent e) {
        String schedTitle = schedTitleTextField.getText();
        setSchedSettings();
        if(isEditView) {
            updateSchedule();
        } else {
            publishNewSchedule(this.schedTitle);
        }
        populateSchedTitleComboBox();
        populateComponents();
        populateMainView();
        dispose();
    }

    private void populateMainView() {
        mainView.populateScheduleMenu();
    }

    private void setSchedSettings() {
        String schedTitle = schedTitleTextField.getText();
        String startTime = constructStartTime();
        String freeRun = String.valueOf(freeRunCheckBox.isSelected());

        searchSettingsMap.put("scheduleTitle", schedTitle);
        searchSettingsMap.put("startTime", startTime);
        searchSettingsMap.put("freeRun", freeRun);
    }

    private String constructStartTime() {
        String startTime = "";
        String hour = startTimeHourTextField.getText();
        String minute = startTimeMinuteTextField.getText();
        String amPm = String.valueOf(startTimeAmPmComboBox.getSelectedItem());

        startTime = hour + ":" + minute + " " + amPm;

        return startTime;
    }

    private void updateSchedule() {
        String schedFolder = Schedule.fetchSchedFolderBySchedTitle(schedTitle);
                try {
            // republish schedule options
            Preferences _prefs = Preferences.userRoot().node("OkcAccounts");
            String[] childrenNames = _prefs.childrenNames();
            for (int i = 0; i < childrenNames.length; i++) {
                _prefs = Preferences.userRoot().node("OkcAccounts");
                String childName = childrenNames[i];
                if(childName.startsWith("schedule")) {
                    _prefs = Preferences.userRoot().node("OkcAccounts/" + childName);
                    String scheduleTitle = _prefs.get("scheduleTitle", "");
                    if(scheduleTitle.equals(this.schedTitle)) {
                        Iterator iterator = searchSettingsMap.keySet().iterator();
                        while(iterator.hasNext()) {
                            String key = String.valueOf(iterator.next());
                            String value = String.valueOf(searchSettingsMap.get(key));
                            _prefs.put(key, value);
                        }
                    } else {
                        continue;
                    }

                    if(!isEditView) {
                        String newScheduleTitleTextValue = schedTitleTextField.getText();
                        searchSettingsMap = fetchSearchSettings(newScheduleTitleTextValue);
                    } else {
                        searchSettingsMap = fetchSearchSettings(scheduleTitle);
                    }

                    // repopulate title comboBox and option labels
                    utils.populateComponents(searchPreferencesView.panel1, searchSettingsMap);
                    utils.populateComponents(panel1, searchSettingsMap);

                    // repopulate mainView.schedules
                    mainView.populateScheduleMenu();
                    break;

                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        // republish title comboBox, option labels and schedule-related search preferences
        Utilities.publishMap(searchSettingsMap, _prefs);
        this.searchSettingsMap = fetchSearchSettings(schedTitle);
        isEditView = false;
    }

    private void publishNewSchedule(String schedTitle) {
        searchSettingsMap = searchPreferencesView.getSearchSettingsMap();
        publishNewScheduleFolder();
        searchSettingsMap.put("scheduleTitle", schedTitleTextField.getText());
        searchSettingsMap.put("startTime", constructStartTime());
        searchSettingsMap.put("freeRun", freeRunCheckBox.isSelected());
        publishScheduleSettings();
    }

    private void publishScheduleSettings() {
        utils.populateMap(searchPreferencesView.panel1, searchSettingsMap);
        Iterator iterator = searchSettingsMap.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = String.valueOf(searchSettingsMap.get(key));
            _prefs.put(key, value);
        }
    }

    private void publishNewScheduleFolder() {
        int schedCount = fetchScheduleCount();
        int nextIndex = 1;
        String schedFolder = "schedule1";

        if(schedCount > 0) {
            try {
                _prefs = Preferences.userRoot().node("OkcAccounts");
                String[] childrenNames = _prefs.childrenNames();

                for (int i = 0; i < childrenNames.length; i++) {
                    String childName = childrenNames[i];
                    if(childName.startsWith("schedule")) {
                        nextIndex++;
                        schedFolder = "schedule" + nextIndex;
                    }
                }
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }

        _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
    }

//    private void publishSchedule() {
//
//        // create the os user preferences folder for the schedule
//        int scheduleCount = getScheduleCount();
//        int nextScheduleCount = scheduleCount+1;
//        String scheduleIndex = "schedule" + nextScheduleCount;
//        try {
//            String[] childrenNames = _prefs.childrenNames();
//            ArrayList scheduleIndexNames = new ArrayList();
//            for (int i = 0; i < childrenNames.length; i++) {
//                String childName = childrenNames[i];
//                if(childName.startsWith("schedule")) {
//                    scheduleIndexNames.add(childName);
//                }
//            }
//
//            while(scheduleIndexNames.contains(scheduleIndex)) {
//                nextScheduleCount++;
//                scheduleIndex = "schedule" + nextScheduleCount;
//            }
//        } catch (BackingStoreException e) {
//            e.printStackTrace();
//        }
//
//        _prefs = Preferences.userRoot().node("OkcAccounts/" + scheduleIndex);
//
//        searchSettingsMap = searchPreferencesView.getSearchSettingsMap();
//        utils.populateMap(searchPreferencesView.panel1, searchSettingsMap);
//        scheduleTitle = schedTitleTextField.getText();
//        String hour = String.valueOf(hourComboBox.getSelectedItem());
//        String minute = String.valueOf(minuteComboBox.getSelectedItem());
//        String amPm = String.valueOf(amPmComboBox.getSelectedItem());
//        String startTime = hour + ":" + minute + " " + amPm;
//        String freeRun = String.valueOf(freeRunCheckBox.isSelected());
//
//        Iterator iterator = searchSettingsMap.keySet().iterator();
//        while(iterator.hasNext()) {
//            String key = String.valueOf(iterator.next());
//            String value = searchSettingsMap.get(key);
//            _prefs.put(key, value);
//        }
//        _prefs.put("scheduleTitle", scheduleTitle);
//        _prefs.put("startTime", startTime);
//        _prefs.put("freeRun", freeRun);
//    }

    private String nextSchedFolderName() {
        _prefs = Preferences.userRoot().node("OkcAccounts");
        ArrayList schedules = new ArrayList();
        String folderName = "";
        try {
            String[] childrenNames = _prefs.childrenNames();
            for (int i = 0; i < childrenNames.length; i++) {
                String childName = childrenNames[i];
                if(childName.startsWith("schedule")) {
                    schedules.add(childName);
                }
            }
            int folderIndex = 1;
            folderName = "schedule" + folderIndex;
            for (int i = 0; i < schedules.size(); i++) {
                folderName = "schedule" + folderIndex;
                if(schedules.contains(folderName)) {
                    folderIndex++;
                } else {
                    break;
                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return folderName;
    }

    private void initAddButton() {
        addButton = new JButton();
        addButton.setText("Add");
        buttonPanel.add(addButton, CC.xy(1, 1));
        addButton.setVisible(false);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addButtonActionPerformed(e);
            }
        });
    }

    private void addButtonActionPerformed(ActionEvent e) {
        addSchedule();
        setMainView();
    }

    private void addSchedule () {
        String schedTitle = schedTitleTextField.getText();
        publishNewSchedule(schedTitle);
        mainView.populateScheduleMenu();
        isEditView = false;
    }

    private void initCancelButton() {
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        buttonPanel.add(cancelButton, CC.xy(1, 2));
        cancelButton.setVisible(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed(e);
            }
        });
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        setMainView();
    }

    private void setMainView() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
                "139dlu",
                "100dlu"));
        pack();
        schedTitleComboBox.setVisible(true);
        schedTitleTextField.setVisible(false);

        startTimeInnerPanel1.setVisible(true);
        startTimeInnerPanel2.setVisible(false);

        freeRunLabel.setVisible(true);
        freeRunCheckBox.setVisible(false);

        searchPreferencesButton.setVisible(false);

        editButton.setVisible(true);
        addButton.setVisible(false);
        applyButton.setVisible(false);

        removeButton.setVisible(true);

        closeButton.setVisible(true);
        cancelButton.setVisible(false);

        newButton.setVisible(true);

        populateSchedTitleComboBox();
        populateComponents();
    }



    private void populateSchedTitleComboBox() {
        schedTitleComboBox.removeAllItems();
        ArrayList schedTitles = fetchSchedTitles();
        for (int i = 0; i < schedTitles.size(); i++) {
            String item = String.valueOf(schedTitles.get(i));
            schedTitleComboBox.addItem(item);
        }
    }

    public static ArrayList fetchSchedTitles() {
        ArrayList schedFolders = new ArrayList();
        ArrayList schedTitles = new ArrayList();
        _prefs = Preferences.userRoot().node("OkcAccounts");
        try {
            String[] childrenNames = _prefs.childrenNames();
            for (int i = 0; i < childrenNames.length; i++) {
                String childName = childrenNames[i];
                if(childName.startsWith("schedule")) {
                    schedFolders.add(childName);
                }
            }
            for (int i = 0; i < schedFolders.size(); i++) {
                String schedFolder = String.valueOf(schedFolders.get(i));
                _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
                String schedTitle = _prefs.get("scheduleTitle", "");
                schedTitles.add(schedTitle);
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        return schedTitles;
    }

    private int fetchScheduleCount() {
        int scheduleCount = 0;
        try {
            _prefs = Preferences.userRoot().node("OkcAccounts");
            String[] childrenNames = _prefs.childrenNames();
            for (int i = 0; i < childrenNames.length; i++) {
                String childName = childrenNames[i];
                if(childName.startsWith("schedule")) {
                    scheduleCount++;
                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        return scheduleCount;
    }

    private void newButtonActionPerformed(ActionEvent e) {
        setNewView();
        prefsModified = false;
    }

    private void editButtonActionPerformed(ActionEvent e) {
        schedTitle = String.valueOf(schedTitleComboBox.getSelectedItem());
        setEditView(schedTitle);
    }

    private void setEditView(String schedTitle) {
        getContentPane().setLayout(new FormLayout(
                "139dlu",
                "129dlu"
        ));
        pack();

        isEditView = true;
        initChangeListener();
        // hide combobox and reveal textField
        schedTitleComboBox.setVisible(false);
        schedTitleTextField.setText(schedTitle);
        schedTitleTextField.setVisible(true);

        // hide startTime Labels and reveal startTime
        // text fields and combobox
        startTimeInnerPanel1.setVisible(false);
        startTimeInnerPanel2.setVisible(true);
        populateComponents();

        freeRunLabel.setVisible(false);
        freeRunCheckBox.setVisible(true);
        searchPreferencesButton.setVisible(true);

        editButton.setVisible(false);
        addButton.setVisible(false);
        applyButton.setVisible(true);
        applyButton.setEnabled(false);

        removeButton.setVisible(false);

        closeButton.setVisible(false);
        cancelButton.setVisible(true);

        newButton.setVisible(false);

        initialValues = new HashMap();
        initialValues = utils.generateUserInputMap(panel1, initialValues);
        utils.populateMap(panel1, initialValues);
    }


    private void populateComponents() {
        // schedTitle depends on if combobox or textField is showing
                String schedTitle = "";
        if(isEditView) {
            schedTitle = schedTitleTextField.getText();
        } else {
            schedTitle = String.valueOf(schedTitleComboBox.getSelectedItem());
        }
        String schedFolder = Schedule.fetchSchedFolderBySchedTitle(schedTitle);
                String startTime = fetchStartTime(schedFolder);
        Boolean isFreeRun = fetchIsFreeRun(schedFolder);
        String[] startTimeArray = deconstructStartTime(startTime);

        if(isEditView) {
            startTimeHourTextField.setText(startTimeArray[0]);
            startTimeHourTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            startTimeMinuteTextField.setText(startTimeArray[1]);
            startTimeMinuteTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            startTimeAmPmComboBox.setSelectedItem(startTimeArray[2]);
            freeRunCheckBox.setSelected(isFreeRun);
        } else {
            startTimeHourLabel.setText(startTimeArray[0]);
            startTimeMinuteLabel.setText(startTimeArray[1]);
            startTimeAmPmLabel.setText(startTimeArray[2]);
            if(isFreeRun) {
                freeRunLabel.setText("on");
            } else {
                freeRunLabel.setText("off");
            }
        }
    }

    private Boolean fetchIsFreeRun(String schedFolder) {
        Boolean isFreeRun;
        _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
        isFreeRun = Boolean.valueOf(_prefs.get("freeRun", ""));
        return isFreeRun;
    }

    private String[] deconstructStartTime(String startTime) {
        String[] startTimeArray = new String[3];
        int colonIndex = startTime.indexOf(":");
        String hour = startTime.substring(0, colonIndex);
        String minute = startTime.substring(colonIndex + 1, startTime.length() - 3);
        String amPm = startTime.substring(startTime.length()-2);

        startTimeArray[0] = hour;
        startTimeArray[1] = minute;
        startTimeArray[2] = amPm;

        return startTimeArray;
    }

//    public static String fetchSchedFolderBySchedTitle(String schedTitle) {
//        String schedFolder = "";
//        String[] childrenNames = fetchChildrenNames("OkcAccounts");
//        for (int i = 0; i < childrenNames.length; i++) {
//            String childName = childrenNames[i];
//            if(childName.startsWith("schedule")) {
//                schedFolder = childName;
//                _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
//                String schedTitle2 = _prefs.get("scheduleTitle", "");
//                if(schedTitle2.equals(schedTitle)) {
//                    break;
//                }
//            }
//
//        }
//        return schedFolder;
//    }

//    private static String[] fetchChildrenNames(String path) {
//        String[] childrenNames = {};
//        _prefs = Preferences.userRoot().node(path);
//        try {
//            childrenNames = _prefs.childrenNames();
//        } catch (BackingStoreException e) {
//            e.printStackTrace();
//        }
//        return childrenNames;
//    }

    private String fetchStartTime(String schedFolder) {
        String startTime = "";
        _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
        startTime = _prefs.get("startTime", "");
        return startTime;
    }

    private void closeButtonActionPerformed(ActionEvent e) {
        dispose();
    }

    private void searchPreferencesButtonActionPerformed(ActionEvent e) {
        String schedTitle = schedTitleTextField.getText();
        if(isEditView) {
            searchSettingsMap = fetchSearchSettings(schedTitle);
        }
        JPanel panel1 = searchPreferencesView.panel1;
        populateSearchPreferencesView(panel1, searchSettingsMap);
        searchPreferencesView.setVisible(true);

    }

    private void populateSearchPreferencesView(JPanel jPanel, HashMap searchSettings) {
        Component[] components = jPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            String componentClassName = component.getClass().getName();
            String componentName = component.getName();

            if(componentClassName.equals("javax.swing.JPanel")) {
                JPanel jPanel2 = (JPanel) component;
                populateSearchPreferencesView(jPanel2, searchSettings);
            }

            if(componentClassName.equals("javax.swing.JComboBox")) {
                JComboBox jComboBox = (JComboBox) component;
                String value = String.valueOf(searchSettings.get(componentName));
                jComboBox.setSelectedItem(value);
            }

            if(componentClassName.equals("javax.swing.JTextField")) {
                JTextField jTextField = (JTextField) component;
                String value = String.valueOf(searchSettings.get(componentName));
                if(value.equals("null")) {
                    value = "";
                }
                jTextField.setText(value);
            }

            if(componentClassName.equals("javax.swing.JCheckBox")) {
                JCheckBox jCheckBox = (JCheckBox) component;
                Boolean value = Boolean.valueOf(String.valueOf(searchSettings.get(componentName)));
                jCheckBox.setSelected(value);
            }
        }

    }

    private void removeButtonActionPerformed(ActionEvent e) {
        String schedTitle = String.valueOf(schedTitleComboBox.getSelectedItem());
        removeSchedule(schedTitle);
    }

    private void removeSchedule(String schedTitle) {
        String schedFolder = Schedule.fetchSchedFolderBySchedTitle(schedTitle);
        _prefs = Preferences.userRoot().node("OkcAccounts/" + schedFolder);
        try {
            _prefs.removeNode();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        if(fetchScheduleCount() > 0) {
            populateSchedTitleComboBox();
        } else {
            setNewView();
        }
        mainView.populateScheduleMenu();
    }
    
    public void setSearchSettingsMap(HashMap<String, String> searchSettingsMap) {
        this.searchSettingsMap = searchSettingsMap;
    }

    public JButton getApplyButton() {
        return applyButton;
    }

    public void setPrefsModified(Boolean prefsModified) {
        this.prefsModified = prefsModified;
    }

    private void schedTitleComboBoxActionPerformed(ActionEvent e) {
        populateComponents();
    }

    private void schedTitleComboBoxPopupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // TODO add your code here
    }

    private void schedTitleComboBoxPopupMenuWillBecomeVisible(PopupMenuEvent e) {
        // TODO add your code here
    }

    private void schedTitleComboBoxPropertyChange(PropertyChangeEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        schedTitlePanel = new JPanel();
        label1 = new JLabel();
        schedTitleComboBox = new JComboBox();
        startTimeOuterPanel = new JPanel();
        startTimeInnerPanel1 = new JPanel();
        label9 = new JLabel();
        startTimeHourLabel = new JLabel();
        startTimeColonLabel1 = new JLabel();
        startTimeMinuteLabel = new JLabel();
        startTimeAmPmLabel = new JLabel();
        startTimeInnerPanel2 = new JPanel();
        label10 = new JLabel();
        startTimeHourTextField = new JTextField();
        startTimeColonLabel2 = new JLabel();
        startTimeMinuteTextField = new JTextField();
        startTimeAmPmComboBox = new JComboBox<>();
        freeRunPanel = new JPanel();
        label6 = new JLabel();
        freeRunLabel = new JLabel();
        searchPreferencesPanel = new JPanel();
        searchPreferencesButton = new JButton();
        buttonPanel = new JPanel();
        editButton = new JButton();
        removeButton = new JButton();
        closeButton = new JButton();
        newButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "139dlu",
            "129dlu"));

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(250, 200));
            panel1.setLayout(new FormLayout(
                "123dlu",
                "default, $lgap, top:default, 2*($lgap, default), $lgap, 4dlu:grow"));

            //======== schedTitlePanel ========
            {
                schedTitlePanel.setName("schedTitlePanel");
                schedTitlePanel.setLayout(new FormLayout(
                    "default, $lcgap, default:grow",
                    "default"));

                //---- label1 ----
                label1.setText("Schedule Title:");
                label1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                schedTitlePanel.add(label1, CC.xy(1, 1));

                //---- schedTitleComboBox ----
                schedTitleComboBox.setName("schedTitleComboBox");
                schedTitleComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        schedTitleComboBoxActionPerformed(e);
                    }
                });
                schedTitleComboBox.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {}
                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        schedTitleComboBoxPopupMenuWillBecomeInvisible(e);
                    }
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        schedTitleComboBoxPopupMenuWillBecomeVisible(e);
                    }
                });
                schedTitleComboBox.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        schedTitleComboBoxPropertyChange(e);
                    }
                });
                schedTitlePanel.add(schedTitleComboBox, CC.xy(3, 1));
            }
            panel1.add(schedTitlePanel, CC.xy(1, 1));

            //======== startTimeOuterPanel ========
            {
                startTimeOuterPanel.setName("startTimeOuterPanel");
                startTimeOuterPanel.setLayout(new FormLayout(
                    "default",
                    "default, $lgap, default"));

                //======== startTimeInnerPanel1 ========
                {
                    startTimeInnerPanel1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeInnerPanel1.setName("startTimeInnerPanel1");
                    startTimeInnerPanel1.setLayout(new FormLayout(
                        "46dlu, $lcgap, default, $lcgap, center:2dlu, $lcgap, 10dlu, $lcgap, 11dlu",
                        "default"));

                    //---- label9 ----
                    label9.setText("Start Time:");
                    label9.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeInnerPanel1.add(label9, new CellConstraints(1, 1, 1, 1, CC.RIGHT, CC.DEFAULT, new Insets(0, 0, 0, 4)));

                    //---- startTimeHourLabel ----
                    startTimeHourLabel.setText("00");
                    startTimeHourLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeHourLabel.setName("startTimeHourLabel");
                    startTimeInnerPanel1.add(startTimeHourLabel, CC.xy(3, 1));

                    //---- startTimeColonLabel1 ----
                    startTimeColonLabel1.setText(":");
                    startTimeColonLabel1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeColonLabel1.setName("startTimeColonLabel1");
                    startTimeInnerPanel1.add(startTimeColonLabel1, CC.xy(5, 1));

                    //---- startTimeMinuteLabel ----
                    startTimeMinuteLabel.setText("00");
                    startTimeMinuteLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeMinuteLabel.setName("startTimeMinuteLabel");
                    startTimeInnerPanel1.add(startTimeMinuteLabel, CC.xy(7, 1));

                    //---- startTimeAmPmLabel ----
                    startTimeAmPmLabel.setText("AM");
                    startTimeAmPmLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeAmPmLabel.setName("startTimeAmPmLabel");
                    startTimeInnerPanel1.add(startTimeAmPmLabel, CC.xy(9, 1));
                }
                startTimeOuterPanel.add(startTimeInnerPanel1, CC.xy(1, 1));

                //======== startTimeInnerPanel2 ========
                {
                    startTimeInnerPanel2.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeInnerPanel2.setName("startTimeInnerPanel2");
                    startTimeInnerPanel2.setLayout(new FormLayout(
                        "43dlu, $lcgap, 15dlu, $lcgap, center:2dlu, $lcgap, 17dlu, $lcgap, 35dlu",
                        "default"));

                    //---- label10 ----
                    label10.setText("Start Time:");
                    label10.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeInnerPanel2.add(label10, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                    //---- startTimeHourTextField ----
                    startTimeHourTextField.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                    startTimeHourTextField.setName("startTimeHourTextField");
                    startTimeInnerPanel2.add(startTimeHourTextField, CC.xy(3, 1));

                    //---- startTimeColonLabel2 ----
                    startTimeColonLabel2.setText(":");
                    startTimeColonLabel2.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    startTimeColonLabel2.setName("startTimeColonLabel2");
                    startTimeInnerPanel2.add(startTimeColonLabel2, CC.xy(5, 1));

                    //---- startTimeMinuteTextField ----
                    startTimeMinuteTextField.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                    startTimeMinuteTextField.setName("startTimeMinuteTextField");
                    startTimeInnerPanel2.add(startTimeMinuteTextField, CC.xy(7, 1));

                    //---- startTimeAmPmComboBox ----
                    startTimeAmPmComboBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                    startTimeAmPmComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
                        "AM",
                        "PM"
                    }));
                    startTimeAmPmComboBox.setName("startTimeAmPmComboBox");
                    startTimeInnerPanel2.add(startTimeAmPmComboBox, CC.xy(9, 1));
                }
                startTimeOuterPanel.add(startTimeInnerPanel2, CC.xy(1, 3));
            }
            panel1.add(startTimeOuterPanel, CC.xy(1, 3));

            //======== freeRunPanel ========
            {
                freeRunPanel.setName("freeRunPanel");
                freeRunPanel.setLayout(new FormLayout(
                    "43dlu, 12dlu",
                    "default"));

                //---- label6 ----
                label6.setText("Free Run:");
                label6.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                freeRunPanel.add(label6, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                //---- freeRunLabel ----
                freeRunLabel.setText("off");
                freeRunLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                freeRunLabel.setName("freeRunLabel");
                freeRunPanel.add(freeRunLabel, CC.xy(2, 1, CC.RIGHT, CC.DEFAULT));
            }
            panel1.add(freeRunPanel, CC.xy(1, 5));

            //======== searchPreferencesPanel ========
            {
                searchPreferencesPanel.setName("searchPreferencesPanel");
                searchPreferencesPanel.setLayout(new FormLayout(
                    "default",
                    "default"));

                //---- searchPreferencesButton ----
                searchPreferencesButton.setText("Search Preferences");
                searchPreferencesButton.setName("searchPreferencesButton");
                searchPreferencesButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        searchPreferencesButtonActionPerformed(e);
                    }
                });
                searchPreferencesPanel.add(searchPreferencesButton, CC.xy(1, 1));
            }
            panel1.add(searchPreferencesPanel, CC.xy(1, 7));

            //======== buttonPanel ========
            {
                buttonPanel.setName("buttonPanel");
                buttonPanel.setLayout(new FormLayout(
                    "default, 41dlu",
                    "2*(default)"));

                //---- editButton ----
                editButton.setText("Edit");
                editButton.setName("editButton");
                editButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(editButton, CC.xy(1, 1));

                //---- removeButton ----
                removeButton.setText("Remove");
                removeButton.setName("removeButton");
                removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(removeButton, CC.xy(2, 1));

                //---- closeButton ----
                closeButton.setText("Close");
                closeButton.setName("closeButton");
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        closeButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(closeButton, CC.xy(1, 2));

                //---- newButton ----
                newButton.setText("New");
                newButton.setName("newButton");
                newButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        newButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(newButton, CC.xy(2, 2));
            }
            panel1.add(buttonPanel, CC.xy(1, 9, CC.DEFAULT, CC.TOP));
        }
        contentPane.add(panel1, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.FILL, new Insets(10, 10, 0, 0)));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JPanel schedTitlePanel;
    private JLabel label1;
    private JComboBox schedTitleComboBox;
    private JPanel startTimeOuterPanel;
    private JPanel startTimeInnerPanel1;
    private JLabel label9;
    private JLabel startTimeHourLabel;
    private JLabel startTimeColonLabel1;
    private JLabel startTimeMinuteLabel;
    private JLabel startTimeAmPmLabel;
    private JPanel startTimeInnerPanel2;
    private JLabel label10;
    private JTextField startTimeHourTextField;
    private JLabel startTimeColonLabel2;
    private JTextField startTimeMinuteTextField;
    private JComboBox<String> startTimeAmPmComboBox;
    private JPanel freeRunPanel;
    private JLabel label6;
    private JLabel freeRunLabel;
    private JPanel searchPreferencesPanel;
    private JButton searchPreferencesButton;
    private JPanel buttonPanel;
    private JButton editButton;
    private JButton removeButton;
    private JButton closeButton;
    private JButton newButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
