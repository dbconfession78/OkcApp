import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Fri May 22 16:44:52 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class SearchPreferencesView extends JFrame {
    private Utilities utils;
    public AccountManager accountMgr;
    HashMap<String, String> searchSettingsMap = new HashMap<>();
    private HashMap<String, String> userInputMap = new HashMap<>();;
    private AbstractAction componentAction;
    private KeyListener keyReleased;
    private HashMap initialValues;
    private JButton runButton;
    private boolean hasChanges = false;
    private boolean isSchedule = false;
    private Preferences _prefs;
    private ScheduleView scheduleView;

    public SearchPreferencesView() {
        initComponents();
        onLoad();
    }

    public SearchPreferencesView(ScheduleView scheduleView) {
        this.scheduleView = scheduleView;
        initComponents();
        onLoad();
        isSchedule = true;
    }

    public SearchPreferencesView(AccountManager accountMgr) {
        this.accountMgr = accountMgr;
        initComponents();
        onLoad();
      }

    private void onLoad() {
        utils = new Utilities();
        searchSettingsMap = utils.generateUserInputMap(panel1, searchSettingsMap);

            // apply listener for any user change;
        initChangeListener();
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

    public HashMap<String, String> getSearchSettingsMap() {
        return searchSettingsMap;
    }

    private void closeButtonActionPerformed(ActionEvent e) {
        dispose();
    }

    private void resetButtonActionPerformed(ActionEvent e) {
        utils.resetComponents(panel1);
        minAgeText.setText("18");
        maxAgeText.setText("99");
        runVisitsText.setText("10");
        visitDelayText.setText("10.5");
        apply();
    }

    private void applyButtonActionPerformed(ActionEvent e) {
        apply();
        if(isSchedule) {
            JButton scheduleManagerApplyButton = scheduleView.getApplyButton();
            scheduleManagerApplyButton.setEnabled(true);
        }
        dispose();
    }

    private void apply() {
        searchSettingsMap = utils.generateUserInputMap(panel1, searchSettingsMap);
        utils.populateMap(panel1, searchSettingsMap);

        if (!isSchedule) {
            accountMgr.updateUserPreferences(searchSettingsMap);
            if (hasChanges) {
                runButton.setEnabled(true);
            }
        } else {
            scheduleView.setSearchSettingsMap(searchSettingsMap);
            scheduleView.setPrefsModified(true);
        }
        if (!isSchedule) {
            accountMgr.setLastRunCompleted(true);
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
                 String runVisits = runVisitsText.getText();
                 String visitDelay = visitDelayText.getText();
                 if(!isSchedule) {
                     runButton = accountMgr.mainView.getRunButton();
                 }
                 double visitDelay2 = 0;
                 if(!visitDelay.equals("")) {
                     visitDelay2 = Double.parseDouble(visitDelay);
                 }
                 if(runVisits.equals("") || runVisits.startsWith("0") || visitDelay2 < 6) {
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

    private void thisWindowOpened(WindowEvent e) {
        initialValues = new HashMap();
        initialValues = utils.generateUserInputMap(panel1, initialValues);
        utils.populateMap(panel1, initialValues);
    }

    public void setIsSchedule(boolean isSchedule) {
        this.isSchedule = isSchedule;
    }

    public void setSearchSettingsMap(HashMap searchSettingsMap) {
        this.searchSettingsMap = searchSettingsMap;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        scrollPane1 = new JScrollPane();
        panel1 = new JPanel();
        leftPanel = new JPanel();
        leftInnerPanel = new JPanel();
        orientationContainer = new JPanel();
        label1 = new JLabel();
        orientationCombo = new JComboBox<>();
        radiusContainer = new JPanel();
        label2 = new JLabel();
        radiusCombo = new JComboBox<>();
        lastOnlineContainer = new JPanel();
        label3 = new JLabel();
        lastOnlineCombo = new JComboBox<>();
        panel20 = new JPanel();
        label4 = new JLabel();
        agePanel = new JPanel();
        minAgeText = new JTextField();
        label5 = new JLabel();
        maxAgeText = new JTextField();
        orderByContainer = new JPanel();
        label6 = new JLabel();
        orderByCombo = new JComboBox<>();
        visitsContainer = new JPanel();
        label40 = new JLabel();
        runVisitsText = new JTextField();
        autohideCheckBox = new JCheckBox();
        visitDelayContainer = new JPanel();
        label42 = new JLabel();
        visitDelayText = new JTextField();
        autoDeleteInboxCheckBox = new JCheckBox();
        heightContainer = new JPanel();
        label25 = new JLabel();
        heightPanel = new JPanel();
        minHeightCombo = new JComboBox<>();
        label26 = new JLabel();
        maxHeightCombo = new JComboBox<>();
        separator1 = new JSeparator();
        ethnicityContainer = new JPanel();
        label38 = new JLabel();
        ethnicitySelectionPanel = new JPanel();
        whiteCheckBox = new JCheckBox();
        asianCheckBox = new JCheckBox();
        blackCheckBox = new JCheckBox();
        hispanicCheckBox = new JCheckBox();
        indianCheckBox = new JCheckBox();
        middleEasternCheckBox = new JCheckBox();
        nativeAmericanCheckBox = new JCheckBox();
        pacificIslanderCheckBox = new JCheckBox();
        otherEthnicityCheckBox = new JCheckBox();
        bodyTypeContainer = new JPanel();
        label36 = new JLabel();
        bodyTypeSelectionPanel = new JPanel();
        thinCheckbox = new JCheckBox();
        fitCheckbox = new JCheckBox();
        averageBodyCheckbox = new JCheckBox();
        curvyCheckbox = new JCheckBox();
        jackedCheckbox = new JCheckBox();
        fullFiguredCheckbox = new JCheckBox();
        aLittleExtraCheckbox = new JCheckBox();
        overweightCheckbox = new JCheckBox();
        attractivenessContainer = new JPanel();
        label35 = new JLabel();
        attractivenessSelectionPanel = new JPanel();
        averageAttractivenessCheckbox = new JCheckBox();
        aboveAverageCheckbox = new JCheckBox();
        hotCheckbox = new JCheckBox();
        religionContainer = new JPanel();
        label24 = new JLabel();
        religionSelectionPanel = new JPanel();
        agnosticismCheckBox = new JCheckBox();
        atheismCheckBox = new JCheckBox();
        buddhismCheckBox = new JCheckBox();
        catholicismCheckBox = new JCheckBox();
        christianityCheckBox = new JCheckBox();
        hinduismCheckBox = new JCheckBox();
        judaismCheckBox = new JCheckBox();
        islamCheckBox = new JCheckBox();
        otherReligionCheckBox = new JCheckBox();
        childrenContainer = new JPanel();
        label32 = new JLabel();
        mainChildrenPanel = new JPanel();
        subChildrenPanel1 = new JPanel();
        wantsCheckBox = new JCheckBox();
        mightWantCheckBox = new JCheckBox();
        doesntWantCheckBox = new JCheckBox();
        separator3 = new JSeparator();
        subChildrenPanel2 = new JPanel();
        hasKidsCheckBox = new JCheckBox();
        doesnthaveCheckBox = new JCheckBox();
        rightPanel = new JPanel();
        advancedFiltersPanel = new JPanel();
        label8 = new JLabel();
        label9 = new JLabel();
        label10 = new JLabel();
        label11 = new JLabel();
        aggressivenessDegCombo = new JComboBox<>();
        aggressivenessImpCombo = new JComboBox<>();
        label12 = new JLabel();
        athleticismDegCombo = new JComboBox<>();
        athleticismImpCombo = new JComboBox<>();
        label13 = new JLabel();
        cockinessDegCombo = new JComboBox<>();
        cockinessImpCombo = new JComboBox<>();
        label14 = new JLabel();
        dorkinessDegCombo = new JComboBox<>();
        dorkinessImpCombo = new JComboBox<>();
        label15 = new JLabel();
        independenceDegCombo = new JComboBox<>();
        independenceImpCombo = new JComboBox<>();
        label16 = new JLabel();
        indieDegCombo = new JComboBox<>();
        indieImpCombo = new JComboBox<>();
        label17 = new JLabel();
        introversionDegCombo = new JComboBox<>();
        introversionImpCombo = new JComboBox<>();
        label18 = new JLabel();
        oldFashinednessDegCombo = new JComboBox<>();
        oldFashinednessImpCombo = new JComboBox<>();
        label19 = new JLabel();
        planningDegCombo = new JComboBox<>();
        planningImpCombo = new JComboBox<>();
        label20 = new JLabel();
        politicalDegCombo = new JComboBox<>();
        politicalImpCombo = new JComboBox<>();
        label21 = new JLabel();
        sexualExperienceDegCombo = new JComboBox<>();
        sexualExperienceImpCombo = new JComboBox<>();
        label22 = new JLabel();
        sociallyFreeDegCombo = new JComboBox<>();
        sociallyFreeImpCombo = new JComboBox<>();
        label23 = new JLabel();
        spiritualityDegCombo = new JComboBox<>();
        spiritualityImpCombo = new JComboBox<>();
        educationContainer = new JPanel();
        label33 = new JLabel();
        educationSelectionPanel = new JPanel();
        highschoolCheckBox = new JCheckBox();
        twoyearCollegeCheckBox = new JCheckBox();
        universityCheckBox = new JCheckBox();
        postGradCheckBox = new JCheckBox();
        smokesContainer = new JPanel();
        label27 = new JLabel();
        smokesSelectionPanel = new JPanel();
        yesCheckBox = new JCheckBox();
        noCheckBox = new JCheckBox();
        whenDrinkingCheckBox = new JCheckBox();
        sometimesSmokesCheckBox = new JCheckBox();
        tryingToQuitCheckBox = new JCheckBox();
        drinksContainer = new JPanel();
        label28 = new JLabel();
        drinksSelectionPanel = new JPanel();
        sociallyCheckBox = new JCheckBox();
        oftenDrinksCheckBox = new JCheckBox();
        rarelyCheckBox = new JCheckBox();
        notAtAllCheckBox = new JCheckBox();
        desperatelyCheckBox = new JCheckBox();
        veryOftenCheckBox = new JCheckBox();
        drugsContainer = new JPanel();
        label29 = new JLabel();
        drugsSelectionPanel = new JPanel();
        everCheckBox = new JCheckBox();
        sometimesDrugsCheckBox = new JCheckBox();
        oftenDrugsCheckBox = new JCheckBox();
        buttonPanel = new JPanel();
        closeButton = new JButton();
        resetButton = new JButton();
        applyButton = new JButton();

        //======== this ========
        setTitle("Search Settings");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                thisWindowOpened(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "337dlu",
            "467dlu"));

        //======== scrollPane1 ========
        {
            scrollPane1.setMinimumSize(new Dimension(23, 780));
            scrollPane1.setPreferredSize(new Dimension(619, 759));

            //======== panel1 ========
            {
                panel1.setPreferredSize(new Dimension(600, 746));
                panel1.setName("panel1");
                panel1.setLayout(new FormLayout(
                    "165dlu, left:166dlu",
                    "top:442dlu, $lgap, default:grow"));

                //======== leftPanel ========
                {
                    leftPanel.setName("leftPanel");
                    leftPanel.setPreferredSize(new Dimension(297, 1000));
                    leftPanel.setLayout(new FormLayout(
                        "163dlu",
                        "top:min"));

                    //======== leftInnerPanel ========
                    {
                        leftInnerPanel.setPreferredSize(new Dimension(3245, 800));
                        leftInnerPanel.setName("leftInnerPanel");
                        leftInnerPanel.setLayout(new FormLayout(
                            "left:155dlu",
                            "7*(default, $lgap), default, 13dlu, 4*(default, $lgap), default"));

                        //======== orientationContainer ========
                        {
                            orientationContainer.setName("orientationContainer");
                            orientationContainer.setLayout(new FormLayout(
                                "default, $lcgap, 101dlu",
                                "default"));

                            //---- label1 ----
                            label1.setText("Search for:");
                            label1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            orientationContainer.add(label1, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //---- orientationCombo ----
                            orientationCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            orientationCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                                "men who like women",
                                "women who like men"
                            }));
                            orientationCombo.setName("orientation");
                            orientationContainer.add(orientationCombo, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(orientationContainer, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                        //======== radiusContainer ========
                        {
                            radiusContainer.setName("radiusContainer");
                            radiusContainer.setLayout(new FormLayout(
                                "default, $lcgap, 72dlu",
                                "default"));

                            //---- label2 ----
                            label2.setText("Radius:");
                            label2.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            radiusContainer.add(label2, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //---- radiusCombo ----
                            radiusCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            radiusCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                                "Anywhere",
                                "5",
                                "10",
                                "25",
                                "50",
                                "100",
                                "250",
                                "500"
                            }));
                            radiusCombo.setPreferredSize(new Dimension(150, 27));
                            radiusCombo.setName("radius");
                            radiusContainer.add(radiusCombo, CC.xy(3, 1, CC.LEFT, CC.DEFAULT));
                        }
                        leftInnerPanel.add(radiusContainer, CC.xy(1, 3, CC.CENTER, CC.DEFAULT));

                        //======== lastOnlineContainer ========
                        {
                            lastOnlineContainer.setName("lastOnlineContainer");
                            lastOnlineContainer.setLayout(new FormLayout(
                                "default, $lcgap, 82dlu",
                                "default"));

                            //---- label3 ----
                            label3.setText("Last online:");
                            label3.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            lastOnlineContainer.add(label3, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //---- lastOnlineCombo ----
                            lastOnlineCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            lastOnlineCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                                "online now",
                                "past day",
                                "past week",
                                "past month",
                                "past year"
                            }));
                            lastOnlineCombo.setName("last_login");
                            lastOnlineContainer.add(lastOnlineCombo, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(lastOnlineContainer, CC.xy(1, 5, CC.CENTER, CC.DEFAULT));

                        //======== panel20 ========
                        {
                            panel20.setLayout(new FormLayout(
                                "default, $lcgap, 101dlu",
                                "default"));

                            //---- label4 ----
                            label4.setText("Ages:");
                            label4.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            panel20.add(label4, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //======== agePanel ========
                            {
                                agePanel.setName("agePanel");
                                agePanel.setLayout(new FormLayout(
                                    "3*(default)",
                                    "default"));

                                //---- minAgeText ----
                                minAgeText.setPreferredSize(new Dimension(50, 28));
                                minAgeText.setText("18");
                                minAgeText.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                                minAgeText.setName("minimum_age");
                                agePanel.add(minAgeText, CC.xy(1, 1, CC.LEFT, CC.DEFAULT));

                                //---- label5 ----
                                label5.setText("-");
                                agePanel.add(label5, CC.xy(2, 1, CC.CENTER, CC.CENTER));

                                //---- maxAgeText ----
                                maxAgeText.setPreferredSize(new Dimension(50, 28));
                                maxAgeText.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                                maxAgeText.setName("maximum_age");
                                maxAgeText.setText("99");
                                agePanel.add(maxAgeText, CC.xy(3, 1));
                            }
                            panel20.add(agePanel, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(panel20, CC.xy(1, 7, CC.RIGHT, CC.DEFAULT));

                        //======== orderByContainer ========
                        {
                            orderByContainer.setName("orderByContainer");
                            orderByContainer.setLayout(new FormLayout(
                                "default, $lcgap, 101dlu",
                                "default"));

                            //---- label6 ----
                            label6.setText("Sort by:");
                            label6.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            orderByContainer.add(label6, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //---- orderByCombo ----
                            orderByCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            orderByCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                                "Login",
                                "Join",
                                "Match",
                                "Match and Login",
                                "Match and Distance",
                                "Enemy",
                                "Special blend",
                                "Random"
                            }));
                            orderByCombo.setPreferredSize(new Dimension(150, 27));
                            orderByCombo.setName("order_by");
                            orderByContainer.add(orderByCombo, CC.xy(3, 1, CC.LEFT, CC.DEFAULT));
                        }
                        leftInnerPanel.add(orderByContainer, CC.xy(1, 9, CC.RIGHT, CC.DEFAULT));

                        //======== visitsContainer ========
                        {
                            visitsContainer.setName("visitsContainer");
                            visitsContainer.setLayout(new FormLayout(
                                "44dlu, $lcgap, left:28dlu, $lcgap, 72dlu",
                                "default"));

                            //---- label40 ----
                            label40.setText("# Visits:");
                            label40.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            visitsContainer.add(label40, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //---- runVisitsText ----
                            runVisitsText.setPreferredSize(new Dimension(50, 28));
                            runVisitsText.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            runVisitsText.setName("limit");
                            runVisitsText.setText("50");
                            visitsContainer.add(runVisitsText, CC.xy(3, 1));

                            //---- autohideCheckBox ----
                            autohideCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            autohideCheckBox.setName("autoHide");
                            autohideCheckBox.setText("Auto-Hide");
                            visitsContainer.add(autohideCheckBox, CC.xy(5, 1, CC.LEFT, CC.DEFAULT));
                        }
                        leftInnerPanel.add(visitsContainer, CC.xy(1, 11, CC.RIGHT, CC.DEFAULT));

                        //======== visitDelayContainer ========
                        {
                            visitDelayContainer.setName("visitDelayContainer");
                            visitDelayContainer.setLayout(new FormLayout(
                                "default, $lcgap, left:28dlu, $lcgap, 72dlu",
                                "default"));

                            //---- label42 ----
                            label42.setText("Visit Delay:");
                            label42.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            visitDelayContainer.add(label42, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //---- visitDelayText ----
                            visitDelayText.setPreferredSize(new Dimension(50, 28));
                            visitDelayText.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            visitDelayText.setText("10.5");
                            visitDelayText.setName("visitDelay");
                            visitDelayContainer.add(visitDelayText, CC.xy(3, 1));

                            //---- autoDeleteInboxCheckBox ----
                            autoDeleteInboxCheckBox.setText("Auto-delete inbox");
                            autoDeleteInboxCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            autoDeleteInboxCheckBox.setName("autoDeleteInbox");
                            visitDelayContainer.add(autoDeleteInboxCheckBox, CC.xy(5, 1, CC.FILL, CC.DEFAULT));
                        }
                        leftInnerPanel.add(visitDelayContainer, CC.xy(1, 13, CC.RIGHT, CC.DEFAULT));

                        //======== heightContainer ========
                        {
                            heightContainer.setName("heightContainer");
                            heightContainer.setLayout(new FormLayout(
                                "default, $lcgap, 101dlu",
                                "default"));

                            //---- label25 ----
                            label25.setText("Height:");
                            label25.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            heightContainer.add(label25, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                            //======== heightPanel ========
                            {
                                heightPanel.setPreferredSize(new Dimension(158, 25));
                                heightPanel.setName("heightPanel");
                                heightPanel.setLayout(new FormLayout(
                                    "default, center:default, default",
                                    "default"));

                                //---- minHeightCombo ----
                                minHeightCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                                minHeightCombo.setPreferredSize(new Dimension(75, 28));
                                minHeightCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                                    "5'0\"",
                                    "5'1\"",
                                    "5'2\"",
                                    "5'3\"",
                                    "5'4\"",
                                    "5'5\"",
                                    "5'6\"",
                                    "5'7\"",
                                    "5'8\"",
                                    "5'9\"",
                                    "5'10\"",
                                    "5'11\"",
                                    "6'0\"",
                                    "6'1\"",
                                    "6'2\"",
                                    "6'3\"",
                                    "6'4\""
                                }));
                                minHeightCombo.setName("minimum_height");
                                heightPanel.add(minHeightCombo, CC.xy(1, 1));

                                //---- label26 ----
                                label26.setText("-");
                                heightPanel.add(label26, CC.xy(2, 1, CC.CENTER, CC.CENTER));

                                //---- maxHeightCombo ----
                                maxHeightCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                                maxHeightCombo.setPreferredSize(new Dimension(75, 28));
                                maxHeightCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                                    "6'4\"",
                                    "6'3\"",
                                    "6'2\"",
                                    "6'1\"",
                                    "6'0\"",
                                    "5'11\"",
                                    "5'10\"",
                                    "5'9\"",
                                    "5'8\"",
                                    "5'7\"",
                                    "5'6\"",
                                    "5'5\"",
                                    "5'4\"",
                                    "5'3\"",
                                    "5'2\"",
                                    "5'1\"",
                                    "5'0\""
                                }));
                                maxHeightCombo.setName("maximum_height");
                                heightPanel.add(maxHeightCombo, CC.xy(3, 1));
                            }
                            heightContainer.add(heightPanel, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(heightContainer, CC.xy(1, 15, CC.RIGHT, CC.DEFAULT));

                        //---- separator1 ----
                        separator1.setForeground(Color.black);
                        leftInnerPanel.add(separator1, CC.xy(1, 16, CC.FILL, CC.DEFAULT));

                        //======== ethnicityContainer ========
                        {
                            ethnicityContainer.setName("ethnicityContainer");
                            ethnicityContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                            ethnicityContainer.setLayout(new FormLayout(
                                "33dlu, $lcgap, 120dlu",
                                "default"));

                            //---- label38 ----
                            label38.setText("Ethnicity:");
                            label38.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            ethnicityContainer.add(label38, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                            //======== ethnicitySelectionPanel ========
                            {
                                ethnicitySelectionPanel.setName("ethnicitySelectionPanel");
                                ethnicitySelectionPanel.setLayout(new FormLayout(
                                    "default, $lcgap, default",
                                    "5*(default)"));

                                //---- whiteCheckBox ----
                                whiteCheckBox.setText("White");
                                whiteCheckBox.setName("white");
                                whiteCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                ethnicitySelectionPanel.add(whiteCheckBox, CC.xy(1, 1));

                                //---- asianCheckBox ----
                                asianCheckBox.setText("Asian");
                                asianCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                asianCheckBox.setName("asian");
                                ethnicitySelectionPanel.add(asianCheckBox, CC.xy(3, 1));

                                //---- blackCheckBox ----
                                blackCheckBox.setText("Black");
                                blackCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                blackCheckBox.setName("black");
                                ethnicitySelectionPanel.add(blackCheckBox, CC.xy(1, 2));

                                //---- hispanicCheckBox ----
                                hispanicCheckBox.setText("Hispanic");
                                hispanicCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                hispanicCheckBox.setName("hispanic_latin");
                                ethnicitySelectionPanel.add(hispanicCheckBox, CC.xy(3, 2));

                                //---- indianCheckBox ----
                                indianCheckBox.setText("Indian");
                                indianCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                indianCheckBox.setName("indian");
                                ethnicitySelectionPanel.add(indianCheckBox, CC.xy(1, 3));

                                //---- middleEasternCheckBox ----
                                middleEasternCheckBox.setText("Middle-Eastern");
                                middleEasternCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                middleEasternCheckBox.setName("middle_eastern");
                                ethnicitySelectionPanel.add(middleEasternCheckBox, CC.xy(3, 3));

                                //---- nativeAmericanCheckBox ----
                                nativeAmericanCheckBox.setText("Native American");
                                nativeAmericanCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                nativeAmericanCheckBox.setName("native_american");
                                ethnicitySelectionPanel.add(nativeAmericanCheckBox, CC.xy(1, 4));

                                //---- pacificIslanderCheckBox ----
                                pacificIslanderCheckBox.setText("Pacific Islander");
                                pacificIslanderCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                pacificIslanderCheckBox.setName("pacific_islander");
                                ethnicitySelectionPanel.add(pacificIslanderCheckBox, CC.xy(3, 4));

                                //---- otherEthnicityCheckBox ----
                                otherEthnicityCheckBox.setText("Other");
                                otherEthnicityCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                otherEthnicityCheckBox.setName("other_ethnicity");
                                ethnicitySelectionPanel.add(otherEthnicityCheckBox, CC.xy(1, 5));
                            }
                            ethnicityContainer.add(ethnicitySelectionPanel, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(ethnicityContainer, CC.xy(1, 17));

                        //======== bodyTypeContainer ========
                        {
                            bodyTypeContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                            bodyTypeContainer.setName("bodyTypeContainer");
                            bodyTypeContainer.setLayout(new FormLayout(
                                "48dlu, $lcgap, left:105dlu",
                                "default:grow"));

                            //---- label36 ----
                            label36.setText("Body Type:");
                            label36.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            bodyTypeContainer.add(label36, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                            //======== bodyTypeSelectionPanel ========
                            {
                                bodyTypeSelectionPanel.setName("bodyTypeSelectionPanel");
                                bodyTypeSelectionPanel.setLayout(new FormLayout(
                                    "54dlu:grow, $lcgap, default",
                                    "3*(default, $lgap), default"));

                                //---- thinCheckbox ----
                                thinCheckbox.setText("Thin");
                                thinCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                thinCheckbox.setName("thin");
                                bodyTypeSelectionPanel.add(thinCheckbox, CC.xy(1, 1));

                                //---- fitCheckbox ----
                                fitCheckbox.setText("Fit");
                                fitCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                fitCheckbox.setName("fit");
                                bodyTypeSelectionPanel.add(fitCheckbox, CC.xy(3, 1));

                                //---- averageBodyCheckbox ----
                                averageBodyCheckbox.setText("Average");
                                averageBodyCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                averageBodyCheckbox.setPreferredSize(new Dimension(72, 20));
                                averageBodyCheckbox.setName("averageBody");
                                bodyTypeSelectionPanel.add(averageBodyCheckbox, CC.xy(1, 3));

                                //---- curvyCheckbox ----
                                curvyCheckbox.setText("Curvy");
                                curvyCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                curvyCheckbox.setName("curvy");
                                bodyTypeSelectionPanel.add(curvyCheckbox, CC.xy(3, 3));

                                //---- jackedCheckbox ----
                                jackedCheckbox.setText("Jacked");
                                jackedCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                jackedCheckbox.setName("jacked");
                                bodyTypeSelectionPanel.add(jackedCheckbox, CC.xy(1, 5));

                                //---- fullFiguredCheckbox ----
                                fullFiguredCheckbox.setText("Full figured");
                                fullFiguredCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                fullFiguredCheckbox.setName("fullFigured");
                                bodyTypeSelectionPanel.add(fullFiguredCheckbox, CC.xy(3, 5));

                                //---- aLittleExtraCheckbox ----
                                aLittleExtraCheckbox.setText("A little extra");
                                aLittleExtraCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                aLittleExtraCheckbox.setName("aLittleExtra");
                                bodyTypeSelectionPanel.add(aLittleExtraCheckbox, CC.xy(1, 7));

                                //---- overweightCheckbox ----
                                overweightCheckbox.setText("Overweight");
                                overweightCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                overweightCheckbox.setName("overweight");
                                bodyTypeSelectionPanel.add(overweightCheckbox, CC.xy(3, 7));
                            }
                            bodyTypeContainer.add(bodyTypeSelectionPanel, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(bodyTypeContainer, CC.xy(1, 19));

                        //======== attractivenessContainer ========
                        {
                            attractivenessContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                            attractivenessContainer.setName("attractivenessContainer");
                            attractivenessContainer.setLayout(new FormLayout(
                                "right:48dlu, $lcgap, 105dlu",
                                "default"));

                            //---- label35 ----
                            label35.setText("Attractiveness:");
                            label35.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            attractivenessContainer.add(label35, new CellConstraints(1, 1, 1, 1, CC.LEFT, CC.TOP, new Insets(5, 5, 0, 0)));

                            //======== attractivenessSelectionPanel ========
                            {
                                attractivenessSelectionPanel.setName("attractivenessSelectionPanel");
                                attractivenessSelectionPanel.setLayout(new FormLayout(
                                    "default, 67dlu",
                                    "default, $lgap, default"));

                                //---- averageAttractivenessCheckbox ----
                                averageAttractivenessCheckbox.setText("Average");
                                averageAttractivenessCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                averageAttractivenessCheckbox.setName("averageAttractiveness");
                                attractivenessSelectionPanel.add(averageAttractivenessCheckbox, CC.xy(1, 1));

                                //---- aboveAverageCheckbox ----
                                aboveAverageCheckbox.setText("Above Average");
                                aboveAverageCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                aboveAverageCheckbox.setName("aboveAverage");
                                attractivenessSelectionPanel.add(aboveAverageCheckbox, CC.xy(2, 1));

                                //---- hotCheckbox ----
                                hotCheckbox.setText("Hot");
                                hotCheckbox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                hotCheckbox.setName("hot");
                                attractivenessSelectionPanel.add(hotCheckbox, CC.xy(1, 3));
                            }
                            attractivenessContainer.add(attractivenessSelectionPanel, CC.xy(3, 1, CC.RIGHT, CC.DEFAULT));
                        }
                        leftInnerPanel.add(attractivenessContainer, CC.xy(1, 21));

                        //======== religionContainer ========
                        {
                            religionContainer.setName("religionContainer");
                            religionContainer.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            religionContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                            religionContainer.setLayout(new FormLayout(
                                "48dlu, $lcgap, 105dlu",
                                "default"));

                            //---- label24 ----
                            label24.setText("Religion:");
                            label24.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            religionContainer.add(label24, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                            //======== religionSelectionPanel ========
                            {
                                religionSelectionPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                religionSelectionPanel.setLayout(new FormLayout(
                                    "default, $lcgap, default",
                                    "4*(default, $lgap), default"));

                                //---- agnosticismCheckBox ----
                                agnosticismCheckBox.setText("Agnosticism");
                                agnosticismCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                agnosticismCheckBox.setName("agnosticism");
                                religionSelectionPanel.add(agnosticismCheckBox, CC.xy(1, 1));

                                //---- atheismCheckBox ----
                                atheismCheckBox.setText("Atheism");
                                atheismCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                atheismCheckBox.setName("atheism");
                                religionSelectionPanel.add(atheismCheckBox, CC.xy(3, 1));

                                //---- buddhismCheckBox ----
                                buddhismCheckBox.setText("Buddhism");
                                buddhismCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                buddhismCheckBox.setName("buddhism");
                                religionSelectionPanel.add(buddhismCheckBox, CC.xy(1, 3));

                                //---- catholicismCheckBox ----
                                catholicismCheckBox.setText("Catholicism");
                                catholicismCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                catholicismCheckBox.setName("catholicism");
                                religionSelectionPanel.add(catholicismCheckBox, CC.xy(3, 3));

                                //---- christianityCheckBox ----
                                christianityCheckBox.setText("Christianity");
                                christianityCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                christianityCheckBox.setName("christianity");
                                religionSelectionPanel.add(christianityCheckBox, CC.xy(1, 5));

                                //---- hinduismCheckBox ----
                                hinduismCheckBox.setText("Hinduism");
                                hinduismCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                hinduismCheckBox.setName("hinduism");
                                religionSelectionPanel.add(hinduismCheckBox, CC.xy(3, 5));

                                //---- judaismCheckBox ----
                                judaismCheckBox.setText("Judaism");
                                judaismCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                judaismCheckBox.setName("judaism");
                                religionSelectionPanel.add(judaismCheckBox, CC.xy(1, 7));

                                //---- islamCheckBox ----
                                islamCheckBox.setText("Islam");
                                islamCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                islamCheckBox.setName("islam");
                                religionSelectionPanel.add(islamCheckBox, CC.xy(3, 7));

                                //---- otherReligionCheckBox ----
                                otherReligionCheckBox.setText("Other");
                                otherReligionCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                otherReligionCheckBox.setName("other_religion");
                                religionSelectionPanel.add(otherReligionCheckBox, CC.xy(1, 9));
                            }
                            religionContainer.add(religionSelectionPanel, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(religionContainer, CC.xy(1, 23));

                        //======== childrenContainer ========
                        {
                            childrenContainer.setName("childrenContainer");
                            childrenContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                            childrenContainer.setVisible(false);
                            childrenContainer.setLayout(new FormLayout(
                                "48dlu, $lcgap, 105dlu",
                                "default"));

                            //---- label32 ----
                            label32.setText("Children:");
                            label32.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                            childrenContainer.add(label32, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                            //======== mainChildrenPanel ========
                            {
                                mainChildrenPanel.setName("mainChildrenPanel");
                                mainChildrenPanel.setLayout(new FormLayout(
                                    "default",
                                    "default, 5dlu, default"));

                                //======== subChildrenPanel1 ========
                                {
                                    subChildrenPanel1.setName("subChildrenPanel1");
                                    subChildrenPanel1.setLayout(new FormLayout(
                                        "50dlu, $lcgap, default",
                                        "default, $lgap, default"));

                                    //---- wantsCheckBox ----
                                    wantsCheckBox.setText("Wants kids");
                                    wantsCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                    wantsCheckBox.setName("wants_kids");
                                    subChildrenPanel1.add(wantsCheckBox, CC.xy(1, 1));

                                    //---- mightWantCheckBox ----
                                    mightWantCheckBox.setText("Might want");
                                    mightWantCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                    mightWantCheckBox.setName("might_want");
                                    subChildrenPanel1.add(mightWantCheckBox, CC.xy(3, 1));

                                    //---- doesntWantCheckBox ----
                                    doesntWantCheckBox.setText("Doesn't want");
                                    doesntWantCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                    doesntWantCheckBox.setName("doesnt_want");
                                    subChildrenPanel1.add(doesntWantCheckBox, CC.xy(1, 3, CC.LEFT, CC.DEFAULT));
                                }
                                mainChildrenPanel.add(subChildrenPanel1, CC.xy(1, 1));

                                //---- separator3 ----
                                separator3.setForeground(Color.black);
                                mainChildrenPanel.add(separator3, CC.xy(1, 2));

                                //======== subChildrenPanel2 ========
                                {
                                    subChildrenPanel2.setName("subChildrenPanel2");
                                    subChildrenPanel2.setLayout(new FormLayout(
                                        "41dlu, $lcgap, default",
                                        "default"));

                                    //---- hasKidsCheckBox ----
                                    hasKidsCheckBox.setText("Has kids");
                                    hasKidsCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                    hasKidsCheckBox.setName("has_kids");
                                    hasKidsCheckBox.setBorderPaintedFlat(true);
                                    hasKidsCheckBox.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                                    subChildrenPanel2.add(hasKidsCheckBox, CC.xy(1, 1, CC.LEFT, CC.DEFAULT));

                                    //---- doesnthaveCheckBox ----
                                    doesnthaveCheckBox.setText("Doesn't have");
                                    doesnthaveCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                                    doesnthaveCheckBox.setName("doesnt_have");
                                    doesnthaveCheckBox.setBorderPaintedFlat(true);
                                    doesnthaveCheckBox.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                                    subChildrenPanel2.add(doesnthaveCheckBox, CC.xy(3, 1));
                                }
                                mainChildrenPanel.add(subChildrenPanel2, CC.xy(1, 3));
                            }
                            childrenContainer.add(mainChildrenPanel, CC.xy(3, 1));
                        }
                        leftInnerPanel.add(childrenContainer, CC.xy(1, 25));
                    }
                    leftPanel.add(leftInnerPanel, CC.xy(1, 1, CC.LEFT, CC.TOP));
                }
                panel1.add(leftPanel, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(10, 10, 0, 0)));

                //======== rightPanel ========
                {
                    rightPanel.setPreferredSize(new Dimension(344, 1000));
                    rightPanel.setLayout(new FormLayout(
                        "165dlu",
                        "164dlu, 8dlu, 35dlu, 4*($lgap, default)"));

                    //======== advancedFiltersPanel ========
                    {
                        advancedFiltersPanel.setPreferredSize(new Dimension(344, 432));
                        advancedFiltersPanel.setMinimumSize(new Dimension(314, 390));
                        advancedFiltersPanel.setName("advancedFiltersPanel");
                        advancedFiltersPanel.setVisible(false);
                        advancedFiltersPanel.setLayout(new FormLayout(
                            "default, $lcgap, left:default, $lcgap, left:55dlu",
                            "13*(pref, $lgap), pref"));

                        //---- label8 ----
                        label8.setText("Personality");
                        advancedFiltersPanel.add(label8, CC.xy(1, 1, CC.LEFT, CC.DEFAULT));

                        //---- label9 ----
                        label9.setText("Degree");
                        advancedFiltersPanel.add(label9, new CellConstraints(3, 1, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                        //---- label10 ----
                        label10.setText("Importance");
                        advancedFiltersPanel.add(label10, new CellConstraints(5, 1, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                        //---- label11 ----
                        label11.setText("Agressiveness");
                        label11.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label11, CC.xy(1, 3));

                        //---- aggressivenessDegCombo ----
                        aggressivenessDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        aggressivenessDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        aggressivenessDegCombo.setPreferredSize(new Dimension(90, 22));
                        aggressivenessDegCombo.setName("aggressivenessDeg");
                        advancedFiltersPanel.add(aggressivenessDegCombo, CC.xy(3, 3));

                        //---- aggressivenessImpCombo ----
                        aggressivenessImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        aggressivenessImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        aggressivenessImpCombo.setPreferredSize(new Dimension(90, 22));
                        aggressivenessImpCombo.setName("aggressivenessImp");
                        advancedFiltersPanel.add(aggressivenessImpCombo, CC.xy(5, 3));

                        //---- label12 ----
                        label12.setText("Athleticisim");
                        label12.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label12, CC.xy(1, 5));

                        //---- athleticismDegCombo ----
                        athleticismDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        athleticismDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        athleticismDegCombo.setPreferredSize(new Dimension(90, 22));
                        athleticismDegCombo.setName("athleticismDeg");
                        advancedFiltersPanel.add(athleticismDegCombo, CC.xy(3, 5));

                        //---- athleticismImpCombo ----
                        athleticismImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        athleticismImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        athleticismImpCombo.setPreferredSize(new Dimension(90, 22));
                        athleticismImpCombo.setName("athleticismImp");
                        advancedFiltersPanel.add(athleticismImpCombo, CC.xy(5, 5));

                        //---- label13 ----
                        label13.setText("Cockiness");
                        label13.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label13, CC.xy(1, 7));

                        //---- cockinessDegCombo ----
                        cockinessDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        cockinessDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        cockinessDegCombo.setPreferredSize(new Dimension(90, 22));
                        cockinessDegCombo.setName("cockinessDeg");
                        advancedFiltersPanel.add(cockinessDegCombo, CC.xy(3, 7));

                        //---- cockinessImpCombo ----
                        cockinessImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        cockinessImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        cockinessImpCombo.setPreferredSize(new Dimension(90, 22));
                        cockinessImpCombo.setName("cockinessImp");
                        advancedFiltersPanel.add(cockinessImpCombo, CC.xy(5, 7));

                        //---- label14 ----
                        label14.setText("Dorkiness");
                        label14.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label14, CC.xy(1, 9));

                        //---- dorkinessDegCombo ----
                        dorkinessDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        dorkinessDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        dorkinessDegCombo.setPreferredSize(new Dimension(90, 22));
                        dorkinessDegCombo.setName("dorkinessDeg");
                        advancedFiltersPanel.add(dorkinessDegCombo, CC.xy(3, 9));

                        //---- dorkinessImpCombo ----
                        dorkinessImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        dorkinessImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        dorkinessImpCombo.setPreferredSize(new Dimension(90, 22));
                        dorkinessImpCombo.setName("dorkinessImp");
                        advancedFiltersPanel.add(dorkinessImpCombo, CC.xy(5, 9));

                        //---- label15 ----
                        label15.setText("Independence");
                        label15.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label15, CC.xy(1, 11));

                        //---- independenceDegCombo ----
                        independenceDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        independenceDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        independenceDegCombo.setPreferredSize(new Dimension(90, 22));
                        independenceDegCombo.setName("independenceDeg");
                        advancedFiltersPanel.add(independenceDegCombo, CC.xy(3, 11));

                        //---- independenceImpCombo ----
                        independenceImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        independenceImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        independenceImpCombo.setPreferredSize(new Dimension(90, 22));
                        independenceImpCombo.setName("independenceImp");
                        advancedFiltersPanel.add(independenceImpCombo, CC.xy(5, 11));

                        //---- label16 ----
                        label16.setText("Indie");
                        label16.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label16, CC.xy(1, 13));

                        //---- indieDegCombo ----
                        indieDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        indieDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        indieDegCombo.setPreferredSize(new Dimension(90, 22));
                        indieDegCombo.setName("indieDeg");
                        advancedFiltersPanel.add(indieDegCombo, CC.xy(3, 13));

                        //---- indieImpCombo ----
                        indieImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        indieImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        indieImpCombo.setPreferredSize(new Dimension(90, 22));
                        indieImpCombo.setName("indieImp");
                        advancedFiltersPanel.add(indieImpCombo, CC.xy(5, 13));

                        //---- label17 ----
                        label17.setText("Introversion");
                        label17.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label17, CC.xy(1, 15));

                        //---- introversionDegCombo ----
                        introversionDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        introversionDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        introversionDegCombo.setPreferredSize(new Dimension(90, 22));
                        introversionDegCombo.setName("introversionDeg");
                        advancedFiltersPanel.add(introversionDegCombo, CC.xy(3, 15));

                        //---- introversionImpCombo ----
                        introversionImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        introversionImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        introversionImpCombo.setPreferredSize(new Dimension(90, 22));
                        introversionImpCombo.setName("introversionImp");
                        advancedFiltersPanel.add(introversionImpCombo, CC.xy(5, 15));

                        //---- label18 ----
                        label18.setText("Old Fashionedness");
                        label18.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label18, CC.xy(1, 17));

                        //---- oldFashinednessDegCombo ----
                        oldFashinednessDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        oldFashinednessDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        oldFashinednessDegCombo.setPreferredSize(new Dimension(90, 22));
                        oldFashinednessDegCombo.setName("oldFashinednessDeg");
                        advancedFiltersPanel.add(oldFashinednessDegCombo, CC.xy(3, 17));

                        //---- oldFashinednessImpCombo ----
                        oldFashinednessImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        oldFashinednessImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        oldFashinednessImpCombo.setPreferredSize(new Dimension(90, 22));
                        oldFashinednessImpCombo.setName("oldFashinednessImp");
                        advancedFiltersPanel.add(oldFashinednessImpCombo, CC.xy(5, 17));

                        //---- label19 ----
                        label19.setText("Planning");
                        label19.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label19, CC.xy(1, 19));

                        //---- planningDegCombo ----
                        planningDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        planningDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        planningDegCombo.setPreferredSize(new Dimension(90, 22));
                        planningDegCombo.setName("planningDeg");
                        advancedFiltersPanel.add(planningDegCombo, CC.xy(3, 19));

                        //---- planningImpCombo ----
                        planningImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        planningImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        planningImpCombo.setPreferredSize(new Dimension(90, 22));
                        planningImpCombo.setName("planningImp");
                        advancedFiltersPanel.add(planningImpCombo, CC.xy(5, 19));

                        //---- label20 ----
                        label20.setText("Political");
                        label20.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label20, CC.xy(1, 21));

                        //---- politicalDegCombo ----
                        politicalDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        politicalDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        politicalDegCombo.setPreferredSize(new Dimension(90, 22));
                        politicalDegCombo.setName("politicalDeg");
                        advancedFiltersPanel.add(politicalDegCombo, CC.xy(3, 21));

                        //---- politicalImpCombo ----
                        politicalImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        politicalImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        politicalImpCombo.setPreferredSize(new Dimension(90, 22));
                        politicalImpCombo.setName("politicalImp");
                        advancedFiltersPanel.add(politicalImpCombo, CC.xy(5, 21));

                        //---- label21 ----
                        label21.setText("Sexual Experience");
                        label21.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label21, CC.xy(1, 23));

                        //---- sexualExperienceDegCombo ----
                        sexualExperienceDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        sexualExperienceDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        sexualExperienceDegCombo.setPreferredSize(new Dimension(90, 22));
                        sexualExperienceDegCombo.setName("sexualExperienceDeg");
                        advancedFiltersPanel.add(sexualExperienceDegCombo, CC.xy(3, 23));

                        //---- sexualExperienceImpCombo ----
                        sexualExperienceImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        sexualExperienceImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        sexualExperienceImpCombo.setPreferredSize(new Dimension(90, 22));
                        sexualExperienceImpCombo.setName("sexualExperienceImp");
                        advancedFiltersPanel.add(sexualExperienceImpCombo, CC.xy(5, 23));

                        //---- label22 ----
                        label22.setText("Socially Free");
                        label22.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label22, CC.xy(1, 25));

                        //---- sociallyFreeDegCombo ----
                        sociallyFreeDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        sociallyFreeDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        sociallyFreeDegCombo.setPreferredSize(new Dimension(90, 22));
                        sociallyFreeDegCombo.setName("sociallyFreeDeg");
                        advancedFiltersPanel.add(sociallyFreeDegCombo, CC.xy(3, 25));

                        //---- sociallyFreeImpCombo ----
                        sociallyFreeImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        sociallyFreeImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        sociallyFreeImpCombo.setPreferredSize(new Dimension(90, 22));
                        sociallyFreeImpCombo.setName("sociallyFreeImp");
                        advancedFiltersPanel.add(sociallyFreeImpCombo, CC.xy(5, 25));

                        //---- label23 ----
                        label23.setText("Spirituality");
                        label23.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        advancedFiltersPanel.add(label23, CC.xy(1, 27));

                        //---- spiritualityDegCombo ----
                        spiritualityDegCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "2500",
                            "5000",
                            "7500",
                            "10000"
                        }));
                        spiritualityDegCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        spiritualityDegCombo.setPreferredSize(new Dimension(90, 22));
                        spiritualityDegCombo.setName("spiritualityDeg");
                        advancedFiltersPanel.add(spiritualityDegCombo, CC.xy(3, 27));

                        //---- spiritualityImpCombo ----
                        spiritualityImpCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                            "0",
                            "5",
                            "10"
                        }));
                        spiritualityImpCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        spiritualityImpCombo.setPreferredSize(new Dimension(90, 22));
                        spiritualityImpCombo.setName("spiritualityImp");
                        advancedFiltersPanel.add(spiritualityImpCombo, CC.xy(5, 27));
                    }
                    rightPanel.add(advancedFiltersPanel, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(10, 0, 0, 0)));

                    //======== educationContainer ========
                    {
                        educationContainer.setName("educationContainer");
                        educationContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                        educationContainer.setLayout(new FormLayout(
                            "37dlu, $lcgap, 101dlu:grow",
                            "default"));

                        //---- label33 ----
                        label33.setText("Education:");
                        label33.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        educationContainer.add(label33, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                        //======== educationSelectionPanel ========
                        {
                            educationSelectionPanel.setName("educationSelectionPanel");
                            educationSelectionPanel.setLayout(new FormLayout(
                                "default, $lcgap, default",
                                "default, $lgap, default"));

                            //---- highschoolCheckBox ----
                            highschoolCheckBox.setText("High school");
                            highschoolCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            highschoolCheckBox.setName("high_school");
                            educationSelectionPanel.add(highschoolCheckBox, CC.xy(1, 1));

                            //---- twoyearCollegeCheckBox ----
                            twoyearCollegeCheckBox.setText("2-year college");
                            twoyearCollegeCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            twoyearCollegeCheckBox.setName("two_year_college");
                            educationSelectionPanel.add(twoyearCollegeCheckBox, CC.xy(3, 1));

                            //---- universityCheckBox ----
                            universityCheckBox.setText("University");
                            universityCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            universityCheckBox.setName("college_university");
                            educationSelectionPanel.add(universityCheckBox, CC.xy(1, 3));

                            //---- postGradCheckBox ----
                            postGradCheckBox.setText("Post grad");
                            postGradCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            postGradCheckBox.setName("post_grad");
                            educationSelectionPanel.add(postGradCheckBox, CC.xy(3, 3));
                        }
                        educationContainer.add(educationSelectionPanel, CC.xy(3, 1));
                    }
                    rightPanel.add(educationContainer, CC.xy(1, 3));

                    //======== smokesContainer ========
                    {
                        smokesContainer.setName("smokesContainer");
                        smokesContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                        smokesContainer.setLayout(new FormLayout(
                            "35dlu, $lcgap, 104dlu",
                            "default"));

                        //---- label27 ----
                        label27.setText("Smokes:");
                        label27.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        smokesContainer.add(label27, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                        //======== smokesSelectionPanel ========
                        {
                            smokesSelectionPanel.setName("smokesSelectionPanel");
                            smokesSelectionPanel.setLayout(new FormLayout(
                                "default, $lcgap, default",
                                "2*(default, $lgap), default"));

                            //---- yesCheckBox ----
                            yesCheckBox.setText("Yes");
                            yesCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            yesCheckBox.setName("yes");
                            smokesSelectionPanel.add(yesCheckBox, CC.xy(1, 1));

                            //---- noCheckBox ----
                            noCheckBox.setText("No");
                            noCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            noCheckBox.setName("no");
                            smokesSelectionPanel.add(noCheckBox, CC.xy(3, 1));

                            //---- whenDrinkingCheckBox ----
                            whenDrinkingCheckBox.setText("When drinking");
                            whenDrinkingCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            whenDrinkingCheckBox.setName("when_drinking");
                            smokesSelectionPanel.add(whenDrinkingCheckBox, CC.xy(1, 3));

                            //---- sometimesSmokesCheckBox ----
                            sometimesSmokesCheckBox.setText("Sometimes");
                            sometimesSmokesCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            sometimesSmokesCheckBox.setName("sometimes_smokes");
                            smokesSelectionPanel.add(sometimesSmokesCheckBox, CC.xy(3, 3));

                            //---- tryingToQuitCheckBox ----
                            tryingToQuitCheckBox.setText("Trying to quit");
                            tryingToQuitCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            tryingToQuitCheckBox.setName("trying_to_quit");
                            smokesSelectionPanel.add(tryingToQuitCheckBox, CC.xy(1, 5));
                        }
                        smokesContainer.add(smokesSelectionPanel, CC.xy(3, 1));
                    }
                    rightPanel.add(smokesContainer, CC.xy(1, 5));

                    //======== drinksContainer ========
                    {
                        drinksContainer.setName("drinksContainer");
                        drinksContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                        drinksContainer.setVisible(false);
                        drinksContainer.setLayout(new FormLayout(
                            "35dlu, $lcgap, 101dlu",
                            "default"));

                        //---- label28 ----
                        label28.setText("Drinks:");
                        label28.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        drinksContainer.add(label28, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                        //======== drinksSelectionPanel ========
                        {
                            drinksSelectionPanel.setName("drinksSelectionPanel");
                            drinksSelectionPanel.setLayout(new FormLayout(
                                "default, $lcgap, default",
                                "2*(default, $lgap), default"));

                            //---- sociallyCheckBox ----
                            sociallyCheckBox.setText("Socially");
                            sociallyCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            sociallyCheckBox.setName("socially");
                            drinksSelectionPanel.add(sociallyCheckBox, CC.xy(1, 1));

                            //---- oftenDrinksCheckBox ----
                            oftenDrinksCheckBox.setText("Often");
                            oftenDrinksCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            oftenDrinksCheckBox.setName("often_drinks");
                            drinksSelectionPanel.add(oftenDrinksCheckBox, CC.xy(3, 1));

                            //---- rarelyCheckBox ----
                            rarelyCheckBox.setText("Rarely");
                            rarelyCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            rarelyCheckBox.setName("rarely");
                            drinksSelectionPanel.add(rarelyCheckBox, CC.xy(1, 3));

                            //---- notAtAllCheckBox ----
                            notAtAllCheckBox.setText("Not at all");
                            notAtAllCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            notAtAllCheckBox.setName("not_at_all");
                            drinksSelectionPanel.add(notAtAllCheckBox, CC.xy(3, 3));

                            //---- desperatelyCheckBox ----
                            desperatelyCheckBox.setText("Desperately");
                            desperatelyCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            desperatelyCheckBox.setName("desperately");
                            drinksSelectionPanel.add(desperatelyCheckBox, CC.xy(1, 5));

                            //---- veryOftenCheckBox ----
                            veryOftenCheckBox.setText("Very often");
                            veryOftenCheckBox.setName("very_often");
                            veryOftenCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            drinksSelectionPanel.add(veryOftenCheckBox, CC.xy(3, 5));
                        }
                        drinksContainer.add(drinksSelectionPanel, CC.xy(3, 1));
                    }
                    rightPanel.add(drinksContainer, CC.xy(1, 7));

                    //======== drugsContainer ========
                    {
                        drugsContainer.setName("drugsContainer");
                        drugsContainer.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                        drugsContainer.setVisible(false);
                        drugsContainer.setLayout(new FormLayout(
                            "34dlu, $lcgap, 101dlu",
                            "default"));

                        //---- label29 ----
                        label29.setText("Drugs:");
                        label29.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        drugsContainer.add(label29, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));

                        //======== drugsSelectionPanel ========
                        {
                            drugsSelectionPanel.setName("drugsSelectionPanel");
                            drugsSelectionPanel.setLayout(new FormLayout(
                                "default, $lcgap, default",
                                "2*(default)"));

                            //---- everCheckBox ----
                            everCheckBox.setText("Never");
                            everCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            everCheckBox.setName("never");
                            drugsSelectionPanel.add(everCheckBox, CC.xy(1, 1));

                            //---- sometimesDrugsCheckBox ----
                            sometimesDrugsCheckBox.setText("Sometimes");
                            sometimesDrugsCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            sometimesDrugsCheckBox.setName("sometimes_drugs");
                            drugsSelectionPanel.add(sometimesDrugsCheckBox, CC.xy(3, 1));

                            //---- oftenDrugsCheckBox ----
                            oftenDrugsCheckBox.setText("Often");
                            oftenDrugsCheckBox.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                            oftenDrugsCheckBox.setName("often_drugs");
                            drugsSelectionPanel.add(oftenDrugsCheckBox, CC.xy(1, 2));
                        }
                        drugsContainer.add(drugsSelectionPanel, CC.xy(3, 1));
                    }
                    rightPanel.add(drugsContainer, CC.xy(1, 11));
                }
                panel1.add(rightPanel, CC.xy(2, 1));

                //======== buttonPanel ========
                {
                    buttonPanel.setLayout(new FormLayout(
                        "2*(default, $lcgap), 39dlu",
                        "default"));

                    //---- closeButton ----
                    closeButton.setText("Close");
                    closeButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            closeButtonActionPerformed(e);
                        }
                    });
                    buttonPanel.add(closeButton, CC.xy(1, 1));

                    //---- resetButton ----
                    resetButton.setText("Reset");
                    resetButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            resetButtonActionPerformed(e);
                        }
                    });
                    buttonPanel.add(resetButton, CC.xy(3, 1));

                    //---- applyButton ----
                    applyButton.setText("Apply");
                    applyButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                    applyButton.setPreferredSize(new Dimension(70, 25));
                    applyButton.setEnabled(false);
                    applyButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            applyButtonActionPerformed(e);
                        }
                    });
                    buttonPanel.add(applyButton, CC.xy(5, 1, CC.LEFT, CC.DEFAULT));
                }
                panel1.add(buttonPanel, new CellConstraints(1, 3, 1, 1, CC.FILL, CC.DEFAULT, new Insets(0, 10, 0, 0)));
            }
            scrollPane1.setViewportView(panel1);
        }
        contentPane.add(scrollPane1, CC.xy(1, 1));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    public JPanel panel1;
    private JPanel leftPanel;
    private JPanel leftInnerPanel;
    private JPanel orientationContainer;
    private JLabel label1;
    private JComboBox<String> orientationCombo;
    private JPanel radiusContainer;
    private JLabel label2;
    private JComboBox<String> radiusCombo;
    private JPanel lastOnlineContainer;
    private JLabel label3;
    private JComboBox<String> lastOnlineCombo;
    private JPanel panel20;
    private JLabel label4;
    private JPanel agePanel;
    private JTextField minAgeText;
    private JLabel label5;
    private JTextField maxAgeText;
    private JPanel orderByContainer;
    private JLabel label6;
    private JComboBox<String> orderByCombo;
    private JPanel visitsContainer;
    private JLabel label40;
    private JTextField runVisitsText;
    private JCheckBox autohideCheckBox;
    private JPanel visitDelayContainer;
    private JLabel label42;
    private JTextField visitDelayText;
    private JCheckBox autoDeleteInboxCheckBox;
    private JPanel heightContainer;
    private JLabel label25;
    private JPanel heightPanel;
    private JComboBox<String> minHeightCombo;
    private JLabel label26;
    private JComboBox<String> maxHeightCombo;
    private JSeparator separator1;
    private JPanel ethnicityContainer;
    private JLabel label38;
    private JPanel ethnicitySelectionPanel;
    private JCheckBox whiteCheckBox;
    private JCheckBox asianCheckBox;
    private JCheckBox blackCheckBox;
    private JCheckBox hispanicCheckBox;
    private JCheckBox indianCheckBox;
    private JCheckBox middleEasternCheckBox;
    private JCheckBox nativeAmericanCheckBox;
    private JCheckBox pacificIslanderCheckBox;
    private JCheckBox otherEthnicityCheckBox;
    private JPanel bodyTypeContainer;
    private JLabel label36;
    private JPanel bodyTypeSelectionPanel;
    private JCheckBox thinCheckbox;
    private JCheckBox fitCheckbox;
    private JCheckBox averageBodyCheckbox;
    private JCheckBox curvyCheckbox;
    private JCheckBox jackedCheckbox;
    private JCheckBox fullFiguredCheckbox;
    private JCheckBox aLittleExtraCheckbox;
    private JCheckBox overweightCheckbox;
    private JPanel attractivenessContainer;
    private JLabel label35;
    private JPanel attractivenessSelectionPanel;
    private JCheckBox averageAttractivenessCheckbox;
    private JCheckBox aboveAverageCheckbox;
    private JCheckBox hotCheckbox;
    private JPanel religionContainer;
    private JLabel label24;
    private JPanel religionSelectionPanel;
    private JCheckBox agnosticismCheckBox;
    private JCheckBox atheismCheckBox;
    private JCheckBox buddhismCheckBox;
    private JCheckBox catholicismCheckBox;
    private JCheckBox christianityCheckBox;
    private JCheckBox hinduismCheckBox;
    private JCheckBox judaismCheckBox;
    private JCheckBox islamCheckBox;
    private JCheckBox otherReligionCheckBox;
    private JPanel childrenContainer;
    private JLabel label32;
    private JPanel mainChildrenPanel;
    private JPanel subChildrenPanel1;
    private JCheckBox wantsCheckBox;
    private JCheckBox mightWantCheckBox;
    private JCheckBox doesntWantCheckBox;
    private JSeparator separator3;
    private JPanel subChildrenPanel2;
    private JCheckBox hasKidsCheckBox;
    private JCheckBox doesnthaveCheckBox;
    private JPanel rightPanel;
    private JPanel advancedFiltersPanel;
    private JLabel label8;
    private JLabel label9;
    private JLabel label10;
    private JLabel label11;
    private JComboBox<String> aggressivenessDegCombo;
    private JComboBox<String> aggressivenessImpCombo;
    private JLabel label12;
    private JComboBox<String> athleticismDegCombo;
    private JComboBox<String> athleticismImpCombo;
    private JLabel label13;
    private JComboBox<String> cockinessDegCombo;
    private JComboBox<String> cockinessImpCombo;
    private JLabel label14;
    private JComboBox<String> dorkinessDegCombo;
    private JComboBox<String> dorkinessImpCombo;
    private JLabel label15;
    private JComboBox<String> independenceDegCombo;
    private JComboBox<String> independenceImpCombo;
    private JLabel label16;
    private JComboBox<String> indieDegCombo;
    private JComboBox<String> indieImpCombo;
    private JLabel label17;
    private JComboBox<String> introversionDegCombo;
    private JComboBox<String> introversionImpCombo;
    private JLabel label18;
    private JComboBox<String> oldFashinednessDegCombo;
    private JComboBox<String> oldFashinednessImpCombo;
    private JLabel label19;
    private JComboBox<String> planningDegCombo;
    private JComboBox<String> planningImpCombo;
    private JLabel label20;
    private JComboBox<String> politicalDegCombo;
    private JComboBox<String> politicalImpCombo;
    private JLabel label21;
    private JComboBox<String> sexualExperienceDegCombo;
    private JComboBox<String> sexualExperienceImpCombo;
    private JLabel label22;
    private JComboBox<String> sociallyFreeDegCombo;
    private JComboBox<String> sociallyFreeImpCombo;
    private JLabel label23;
    private JComboBox<String> spiritualityDegCombo;
    private JComboBox<String> spiritualityImpCombo;
    private JPanel educationContainer;
    private JLabel label33;
    private JPanel educationSelectionPanel;
    private JCheckBox highschoolCheckBox;
    private JCheckBox twoyearCollegeCheckBox;
    private JCheckBox universityCheckBox;
    private JCheckBox postGradCheckBox;
    private JPanel smokesContainer;
    private JLabel label27;
    private JPanel smokesSelectionPanel;
    private JCheckBox yesCheckBox;
    private JCheckBox noCheckBox;
    private JCheckBox whenDrinkingCheckBox;
    private JCheckBox sometimesSmokesCheckBox;
    private JCheckBox tryingToQuitCheckBox;
    private JPanel drinksContainer;
    private JLabel label28;
    private JPanel drinksSelectionPanel;
    private JCheckBox sociallyCheckBox;
    private JCheckBox oftenDrinksCheckBox;
    private JCheckBox rarelyCheckBox;
    private JCheckBox notAtAllCheckBox;
    private JCheckBox desperatelyCheckBox;
    private JCheckBox veryOftenCheckBox;
    private JPanel drugsContainer;
    private JLabel label29;
    private JPanel drugsSelectionPanel;
    private JCheckBox everCheckBox;
    private JCheckBox sometimesDrugsCheckBox;
    private JCheckBox oftenDrugsCheckBox;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton resetButton;
    private JButton applyButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}