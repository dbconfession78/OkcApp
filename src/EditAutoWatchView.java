import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Wed Jul 01 18:42:13 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class EditAutoWatchView extends JFrame {
    private AccountManager accountMgr;
    private Preferences _prefs;
    private ArrayList<String> watchedProfiles;
    private String username;
    private String account;
    private Utilities utils = new Utilities();

    public EditAutoWatchView(AccountManager accountMgr) {
        this.accountMgr = accountMgr;
        initComponents();
        onLoad();

    }

    private void onLoad() {
        username = accountMgr.getUsername();
        account = accountMgr.getAccountFromUsername(username);
        watchedProfiles = accountMgr.fetchWatchers();
        populateWatchListPanel();
    }

    private void addButtonActionPerformed(ActionEvent e) {
        String profile = profileNameText.getText();
        String currentLogin = accountMgr.getUsername();
        if(profile.equals(currentLogin)) {
            int accountCount = accountMgr.fetchAccountCount();
            if(accountCount < 2) {
                utils.println("Self-watch requires a second account.");
            } else {
                addWatcher(profile);
            }
        } else {
            addWatcher(profile);
        }

        populateWatchListPanel();
        accountMgr.mainView.populateWatchListPanel(watchedProfiles);
        profileNameText.setText("");
        addButton.setEnabled(false);
    }

    private void populateWatchListPanel() {
        clearWatchListPanel();
        Component[] components = watchListPanel.getComponents();

        for (int i = 0; i < watchedProfiles.size(); i++) {
            for (int j = 0; j < components.length; j++) {
                Component component = components[j];
                String componentClassName = component.getClass().getName();
                if(componentClassName.equals("javax.swing.JLabel")) {
                    JLabel jLabel = (JLabel) component;
                    if(jLabel.getText().equals("")) {
                        jLabel.setText(String.valueOf(watchedProfiles.get(i)));
                        Component component2 = components[j+1];
                        component2.setVisible(true);
                        break;
                    }
                }

            }
        }

    }

    private void clearWatchListPanel() {
        Component[] components = watchListPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            String componentClassName = component.getClass().getName();
            if(componentClassName.equals("javax.swing.JLabel")) {
                JLabel jLabel = (JLabel) component;
                jLabel.setText("");
                Component component2 = components[i+1];
                component2.setVisible(false);
            }
        }
    }

    private void addWatcher(String watcher) {
        watchedProfiles.add(watcher);
        publishWatchers();
    }

    private void publishWatchers() {
        _prefs = Preferences.userRoot().node("OkcAccounts/" + account + "/watchers");
        for (int i = 0; i < watchedProfiles.size(); i++) {
            _prefs.put("watcher" + (i+1), String.valueOf(watchedProfiles.get(i)));
        }
    }

    private void closeButtonActionPerformed(ActionEvent e) {
        this.dispose();
    }

    private void deleteWatch5ButtonActionPerformed(ActionEvent e) {
        deleteWatcher(deleteWatch5Button);
    }

    private void deleteWatch4ButtonActionPerformed(ActionEvent e) {
        deleteWatcher(deleteWatch4Button);
    }

    private void deleteWatch3ButtonActionPerformed(ActionEvent e) {
        deleteWatcher(deleteWatch3Button);
    }

    private void deleteWatch2ButtonActionPerformed(ActionEvent e) {
        deleteWatcher(deleteWatch2Button);
    }

    private void deleteWatch1ButtonActionPerformed(ActionEvent e) {
        deleteWatcher(deleteWatch1Button);
    }

    private void deleteWatcher(JButton deleteWatchButton) {
        Component[] components = watchListPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            String componentName = component.getName();
            if(componentName.equals(deleteWatchButton.getName())) {
                Component component2 = components[i-1];
                JLabel jLabel = (JLabel) component2;
                _prefs = Preferences.userRoot().node("OkcAccounts/" + account + "/watchers");
                try {
                    String[] keys = _prefs.keys();
                    for (int j = 0; j < keys.length; j++) {
                        String key = keys[j];
                        if(_prefs.get(key, "").equals(jLabel.getText())) {
                            _prefs.remove(key);
                            watchedProfiles = accountMgr.fetchWatchers();
                            populateWatchListPanel();
                            accountMgr.mainView.populateWatchListPanel(watchedProfiles);
                            break;
                        }
                    }
                } catch (BackingStoreException e1) {
                    utils.println("\nBackingStoreException");
                    e1.printStackTrace();
                }
            }
        }
    }

    private void deleteWatcher(String watcher) {

    }

    private void profileNameTextKeyReleased(KeyEvent e) {
        int watcherCount = accountMgr.fetchWatchers().size();
        if(watcherCount < 5 && !profileNameText.getText().equals("")) {
            addButton.setEnabled(true);
        } else {
            addButton.setEnabled(false);
        }
    }

    private void clearButtonActionPerformed(ActionEvent e) {
        removeAllWatchers();
        clearWatchListPanel();
        accountMgr.mainView.populateWatchListPanel(watchedProfiles);
    }

    private void removeAllWatchers() {
        try {
            _prefs = Preferences.userRoot().node("OkcAccounts/" + account + "/watchers");
            String[] keys = _prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                _prefs.remove(key);
            }

            watchedProfiles = accountMgr.fetchWatchers();
        } catch (BackingStoreException e) {
            utils.println("\nBackingStoreException");
            e.printStackTrace();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        userInputPanel = new JPanel();
        profileNameText = new JTextField();
        addButton = new JButton();
        separator1 = new JSeparator();
        label7 = new JLabel();
        watchListPanel = new JPanel();
        watch1Label = new JLabel();
        deleteWatch1Button = new JButton();
        watch2Label = new JLabel();
        deleteWatch2Button = new JButton();
        watch3Label = new JLabel();
        deleteWatch3Button = new JButton();
        watch4Label = new JLabel();
        deleteWatch4Button = new JButton();
        watch5Label = new JLabel();
        deleteWatch5Button = new JButton();
        separator2 = new JSeparator();
        buttonPanel = new JPanel();
        closeButton = new JButton();
        clearButton = new JButton();

        //======== this ========
        setTitle("Watched Profiles");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "100dlu, $lcgap, default",
            "148dlu:grow"));

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(200, 240));
            panel1.setLayout(new FormLayout(
                "left:97dlu",
                "fill:min, 2*(default), $lgap, min, 8dlu, 20dlu"));

            //======== userInputPanel ========
            {
                userInputPanel.setName("userInputPanel");
                userInputPanel.setPreferredSize(new Dimension(250, 150));
                userInputPanel.setLayout(new FormLayout(
                    "left:55dlu, 31dlu",
                    "default"));

                //---- profileNameText ----
                profileNameText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                profileNameText.setName("profilename");
                profileNameText.setPreferredSize(new Dimension(100, 25));
                profileNameText.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        profileNameTextKeyReleased(e);
                    }
                });
                userInputPanel.add(profileNameText, CC.xy(1, 1));

                //---- addButton ----
                addButton.setText("Add");
                addButton.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                addButton.setName("addButton");
                addButton.setEnabled(false);
                addButton.setPreferredSize(new Dimension(50, 20));
                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addButtonActionPerformed(e);
                    }
                });
                userInputPanel.add(addButton, new CellConstraints(2, 1, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));
            }
            panel1.add(userInputPanel, CC.xy(1, 1));
            panel1.add(separator1, CC.xy(1, 2, CC.FILL, CC.DEFAULT));

            //---- label7 ----
            label7.setText("Watched Profiles");
            panel1.add(label7, new CellConstraints(1, 3, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

            //======== watchListPanel ========
            {
                watchListPanel.setName("watchListPanel");
                watchListPanel.setPreferredSize(new Dimension(200, 160));
                watchListPanel.setLayout(new FormLayout(
                    "55dlu:grow, 45dlu",
                    "14dlu, $nlgap, 14dlu, $lcgap, 2*(14dlu, $nlgap), 14dlu"));

                //---- watch1Label ----
                watch1Label.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                watch1Label.setName("watch1");
                watchListPanel.add(watch1Label, CC.xy(1, 1));

                //---- deleteWatch1Button ----
                deleteWatch1Button.setText("Delete");
                deleteWatch1Button.setPreferredSize(new Dimension(50, 20));
                deleteWatch1Button.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                deleteWatch1Button.setName("deleteWatch1Button");
                deleteWatch1Button.setVisible(false);
                deleteWatch1Button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteWatch1ButtonActionPerformed(e);
                    }
                });
                watchListPanel.add(deleteWatch1Button, new CellConstraints(2, 1, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                //---- watch2Label ----
                watch2Label.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                watch2Label.setName("watch2");
                watchListPanel.add(watch2Label, CC.xy(1, 3));

                //---- deleteWatch2Button ----
                deleteWatch2Button.setText("Delete");
                deleteWatch2Button.setPreferredSize(new Dimension(50, 20));
                deleteWatch2Button.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                deleteWatch2Button.setName("deleteWatch2Button");
                deleteWatch2Button.setVisible(false);
                deleteWatch2Button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteWatch2ButtonActionPerformed(e);
                    }
                });
                watchListPanel.add(deleteWatch2Button, new CellConstraints(2, 3, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                //---- watch3Label ----
                watch3Label.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                watch3Label.setName("watch3");
                watchListPanel.add(watch3Label, CC.xy(1, 5));

                //---- deleteWatch3Button ----
                deleteWatch3Button.setText("Delete");
                deleteWatch3Button.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                deleteWatch3Button.setPreferredSize(new Dimension(50, 20));
                deleteWatch3Button.setName("deleteWatch3Button");
                deleteWatch3Button.setVisible(false);
                deleteWatch3Button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteWatch3ButtonActionPerformed(e);
                    }
                });
                watchListPanel.add(deleteWatch3Button, new CellConstraints(2, 5, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                //---- watch4Label ----
                watch4Label.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                watch4Label.setName("watch4");
                watchListPanel.add(watch4Label, CC.xy(1, 7));

                //---- deleteWatch4Button ----
                deleteWatch4Button.setText("Delete");
                deleteWatch4Button.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                deleteWatch4Button.setPreferredSize(new Dimension(50, 20));
                deleteWatch4Button.setName("deleteWatch4Button");
                deleteWatch4Button.setVisible(false);
                deleteWatch4Button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteWatch4ButtonActionPerformed(e);
                    }
                });
                watchListPanel.add(deleteWatch4Button, new CellConstraints(2, 7, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));

                //---- watch5Label ----
                watch5Label.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
                watch5Label.setName("watch5");
                watchListPanel.add(watch5Label, CC.xy(1, 9));

                //---- deleteWatch5Button ----
                deleteWatch5Button.setText("Delete");
                deleteWatch5Button.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                deleteWatch5Button.setPreferredSize(new Dimension(50, 20));
                deleteWatch5Button.setName("deleteWatch5Button");
                deleteWatch5Button.setVisible(false);
                deleteWatch5Button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteWatch5ButtonActionPerformed(e);
                    }
                });
                watchListPanel.add(deleteWatch5Button, new CellConstraints(2, 9, 1, 1, CC.LEFT, CC.DEFAULT, new Insets(0, 5, 0, 0)));
            }
            panel1.add(watchListPanel, new CellConstraints(1, 5, 1, 1, CC.DEFAULT, CC.TOP, new Insets(0, 5, 0, 0)));
            panel1.add(separator2, CC.xy(1, 6, CC.FILL, CC.DEFAULT));

            //======== buttonPanel ========
            {
                buttonPanel.setLayout(new FormLayout(
                    "default, 3dlu, default, $lcgap, default",
                    "default"));

                //---- closeButton ----
                closeButton.setText("Close");
                closeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                closeButton.setName("closeButton");
                closeButton.setPreferredSize(new Dimension(50, 20));
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        closeButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(closeButton, CC.xy(1, 1));

                //---- clearButton ----
                clearButton.setText("Clear");
                clearButton.setPreferredSize(new Dimension(50, 20));
                clearButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                clearButton.setName("clearButton");
                clearButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clearButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(clearButton, CC.xy(3, 1));
            }
            panel1.add(buttonPanel, CC.xy(1, 7, CC.FILL, CC.FILL));
        }
        contentPane.add(panel1, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(5, 5, 0, 0)));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    public JPanel panel1;
    private JPanel userInputPanel;
    private JTextField profileNameText;
    private JButton addButton;
    private JSeparator separator1;
    private JLabel label7;
    private JPanel watchListPanel;
    private JLabel watch1Label;
    private JButton deleteWatch1Button;
    private JLabel watch2Label;
    private JButton deleteWatch2Button;
    private JLabel watch3Label;
    private JButton deleteWatch3Button;
    private JLabel watch4Label;
    private JButton deleteWatch4Button;
    private JLabel watch5Label;
    private JButton deleteWatch5Button;
    private JSeparator separator2;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton clearButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
