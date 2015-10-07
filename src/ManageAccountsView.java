import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Tue May 19 17:16:38 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class ManageAccountsView extends JFrame {
    private AccountManager accountMgr;
    private MainView mainView;
    public JButton addAccountButton = new JButton();
    private Utilities utils;
    private HashMap<String, String> loginSettings;
    private AddAccountView addAccountView;
    private HashMap<String, String> initialValues;

    public ManageAccountsView(AccountManager accountMgr) {
        this.accountMgr = accountMgr;
        addAccountView = new AddAccountView(accountMgr);
        initComponents();
        onLoad();
    }

    private void onLoad() {

        applyButton.setEnabled(false);
        mainView = accountMgr.mainView;
        this.utils = new Utilities();

        addAccountButton.setText("Add Account");
        addAccountButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        panel1.add(addAccountButton, CC.xy(3, 1));
        addAccountButton.setVisible(false);
        addAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                accountMgr.showAddAccountView();
            }
        });

        accountMgr.setUsernameComponent(usernameCombo, addAccountButton);
        if(this.isActive() && usernameCombo.isVisible()) {
            accountMgr.populateUsernames(usernameCombo);
        }

        String[] keys = new String[100];
        String[] values = new String[100];


//        for (int i = 0; i < components.length; i++) {
//            Component component = panel1.getComponent(i);
//            String componentName = component.getName();
//            String componentClassName = component.getClass().getName();
//
//            if(componentClassName.equals("javax.swing.JTextField")) {
//                JTextField jTextField = (JTextField) component;
//                keys[i] = componentName;
//                values[i] = jTextField.getText();
//            }
//        }

        while(true) {

            break;
        }
    }

//    public HashMap<String,String> getLoginSettings () {
//        return this.loginSettings;
//    }

    private void deleteButtonActionPerformed(ActionEvent e) {
        accountMgr.deleteAccount(usernameCombo);
        accountMgr.populateUsernames(usernameCombo);
        mainView.accountMgr.populateUsernames(mainView.usernamesCombo);
        if(accountMgr.fetchAccountCount() == 0) {
            dispose();
        }
    }

    private void closeButtonActionPerformed(ActionEvent e) {
        dispose();
    }

    private void usernameComboItemStateChanged(ItemEvent e) {
        String username = String.valueOf(usernameCombo.getSelectedItem());
        utils.populateComponents(panel1, accountMgr.fetchLoginSettings(username));

        getInitialValues();
    }

    private void getInitialValues() {
        initialValues = new HashMap<>();

        Component[] components = panel1.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = panel1.getComponent(i);
            String componentName = component.getName();
            String componentClassName = component.getClass().getName();

            if (componentClassName.equals("javax.swing.JTextField")) {
                JTextField jTextField = (JTextField) component;
                initialValues.put(componentName, jTextField.getText());
            }
        }
    }

    private void applyButtonActionPerformed(ActionEvent e) {
        HashMap<String, String> loginSettingsMap = new HashMap<>();
        loginSettingsMap = utils.generateUserInputMap(panel1, loginSettingsMap);
        utils.populateMap(panel1, loginSettingsMap);
        accountMgr.updateUserPreferences(loginSettingsMap);
    }

    private void addButtonActionPerformed(ActionEvent e) {
        if(!addAccountView.isVisible()) {
            addAccountView.setVisible(true);
        }
    }

    private void passwordTextKeyReleased(KeyEvent e) {
        String initialValue = initialValues.get("password");
        if(initialValue.compareTo(passwordText.getText()) != 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }
    }

    private void zipTextKeyReleased(KeyEvent e) {
        String initialValue = initialValues.get("zip");
        if(initialValue.compareTo(passwordText.getText()) != 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }    }

    private void proxyTextKeyReleased(KeyEvent e) {
        String initialValue = initialValues.get("proxy");
        if(initialValue.compareTo(proxyText.getText()) != 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }    }

    private void portTextKeyReleased(KeyEvent e) {
        String initialValue = initialValues.get("port");
        if(initialValue.compareTo(portText.getText()) != 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }    }

    private void proxyUsernameTextKeyReleased(KeyEvent e) {
        String initialValue = initialValues.get("proxyUsername");
        if(initialValue.compareTo(proxyUsernameText.getText()) != 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }    }

    private void proxyPasswordTextKeyReleased(KeyEvent e) {
        String initialValue = initialValues.get("proxyPassword");
        if(initialValue.compareTo(proxyPasswordText.getText()) != 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        label1 = new JLabel();
        usernameCombo = new JComboBox();
        label2 = new JLabel();
        passwordText = new JTextField();
        label3 = new JLabel();
        zipText = new JTextField();
        label4 = new JLabel();
        proxyText = new JTextField();
        label5 = new JLabel();
        portText = new JTextField();
        label6 = new JLabel();
        proxyUsernameText = new JTextField();
        label7 = new JLabel();
        proxyPasswordText = new JTextField();
        buttonPanel = new JPanel();
        addButton = new JButton();
        deleteButton = new JButton();
        closeButton = new JButton();
        applyButton = new JButton();

        //======== this ========
        setTitle("Login Settings");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "136dlu",
            "top:138dlu, $lgap, default"));

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(200, 250));
            panel1.setLayout(new FormLayout(
                "default, $lcgap, 82dlu",
                "6*(default, $lgap), default"));

            //---- label1 ----
            label1.setText("Username:");
            label1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label1, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

            //---- usernameCombo ----
            usernameCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            usernameCombo.setPreferredSize(new Dimension(120, 27));
            usernameCombo.setName("username");
            usernameCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    usernameComboItemStateChanged(e);
                }
            });
            panel1.add(usernameCombo, CC.xy(3, 1));

            //---- label2 ----
            label2.setText("Password:");
            label2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label2, CC.xy(1, 3, CC.RIGHT, CC.DEFAULT));

            //---- passwordText ----
            passwordText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            passwordText.setPreferredSize(new Dimension(120, 25));
            passwordText.setName("password");
            passwordText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    passwordTextKeyReleased(e);
                }
            });
            panel1.add(passwordText, CC.xy(3, 3));

            //---- label3 ----
            label3.setText("Zip:");
            label3.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label3, CC.xy(1, 5, CC.RIGHT, CC.DEFAULT));

            //---- zipText ----
            zipText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            zipText.setPreferredSize(new Dimension(120, 25));
            zipText.setName("zip");
            zipText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    zipTextKeyReleased(e);
                }
            });
            panel1.add(zipText, CC.xy(3, 5));

            //---- label4 ----
            label4.setText("Proxy:");
            label4.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label4, CC.xy(1, 7, CC.RIGHT, CC.DEFAULT));

            //---- proxyText ----
            proxyText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            proxyText.setPreferredSize(new Dimension(120, 25));
            proxyText.setName("proxy");
            proxyText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    proxyTextKeyReleased(e);
                }
            });
            panel1.add(proxyText, CC.xy(3, 7));

            //---- label5 ----
            label5.setText("Port:");
            label5.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label5, CC.xy(1, 9, CC.RIGHT, CC.DEFAULT));

            //---- portText ----
            portText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            portText.setPreferredSize(new Dimension(120, 25));
            portText.setName("port");
            portText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    portTextKeyReleased(e);
                }
            });
            panel1.add(portText, CC.xy(3, 9));

            //---- label6 ----
            label6.setText("Proxy username:");
            label6.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label6, CC.xy(1, 11, CC.RIGHT, CC.DEFAULT));

            //---- proxyUsernameText ----
            proxyUsernameText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            proxyUsernameText.setPreferredSize(new Dimension(120, 25));
            proxyUsernameText.setName("proxyUsername");
            proxyUsernameText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    proxyUsernameTextKeyReleased(e);
                }
            });
            panel1.add(proxyUsernameText, CC.xy(3, 11));

            //---- label7 ----
            label7.setText("Proxy Password:");
            label7.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            panel1.add(label7, CC.xy(1, 13, CC.RIGHT, CC.DEFAULT));

            //---- proxyPasswordText ----
            proxyPasswordText.setPreferredSize(new Dimension(120, 25));
            proxyPasswordText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
            proxyPasswordText.setName("proxyPassword");
            proxyPasswordText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    proxyPasswordTextKeyReleased(e);
                }
            });
            panel1.add(proxyPasswordText, CC.xy(3, 13));
        }
        contentPane.add(panel1, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.TOP, new Insets(10, 10, 0, 0)));

        //======== buttonPanel ========
        {
            buttonPanel.setLayout(new FormLayout(
                "default, $lcgap, default",
                "2*(default)"));

            //---- addButton ----
            addButton.setText("Add");
            addButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
            addButton.setName("addButton");
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addButtonActionPerformed(e);
                }
            });
            buttonPanel.add(addButton, CC.xy(1, 1));

            //---- deleteButton ----
            deleteButton.setText("Delete");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteButtonActionPerformed(e);
                }
            });
            buttonPanel.add(deleteButton, CC.xy(3, 1));

            //---- closeButton ----
            closeButton.setText("Close");
            closeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
            closeButton.setPreferredSize(new Dimension(86, 29));
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closeButtonActionPerformed(e);
                }
            });
            buttonPanel.add(closeButton, CC.xy(1, 2, CC.LEFT, CC.DEFAULT));

            //---- applyButton ----
            applyButton.setText("Apply");
            applyButton.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
            applyButton.setPreferredSize(new Dimension(86, 29));
            applyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyButtonActionPerformed(e);
                }
            });
            buttonPanel.add(applyButton, CC.xy(3, 2, CC.LEFT, CC.DEFAULT));
        }
        contentPane.add(buttonPanel, CC.xy(1, 3, CC.RIGHT, CC.TOP));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JLabel label1;
    public JComboBox usernameCombo;
    private JLabel label2;
    private JTextField passwordText;
    private JLabel label3;
    private JTextField zipText;
    private JLabel label4;
    private JTextField proxyText;
    private JLabel label5;
    private JTextField portText;
    private JLabel label6;
    private JTextField proxyUsernameText;
    private JLabel label7;
    private JTextField proxyPasswordText;
    private JPanel buttonPanel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton closeButton;
    private JButton applyButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
