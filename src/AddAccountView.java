import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Wed May 20 18:36:47 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class AddAccountView extends JFrame {
    private AccountManager accountMgr;
    private Utilities utils;
    private SearchPreferencesView searchPreferencesView;
    HashMap<String, String> loginSettingsMap = new HashMap<String, String>();
    HashMap<String, String> searchSettingsMap;
    private ActionListener componentAction;
    private KeyListener keyReleased;

    public AddAccountView(AccountManager accountMgr) {
        this.accountMgr = accountMgr;
        initComponents();
        onLoad();

    }

    private void onLoad() {
        utils = new Utilities();
        searchPreferencesView = new SearchPreferencesView(accountMgr);
        loginSettingsMap = utils.generateUserInputMap(panel1, loginSettingsMap);
        searchSettingsMap = searchPreferencesView.getSearchSettingsMap();
        this.requestFocusInWindow();

        componentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // add action if components other than JTextField are added.
            }
        };
        keyReleased = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if(usernameText.getText().equals("") || passwordText.getText().equals("") || zipText.getText().equals("")) {
                    addButton.setEnabled(false);
                } else {
                    addButton.setEnabled(true);
                }
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

    private void closeButtonActionPerformed(ActionEvent e) {
        this.dispose();
    }

    private void addButtonActionPerformed(ActionEvent e) {
        utils.populateMap(panel1, loginSettingsMap);
        this.accountMgr.addAccount(loginSettingsMap, searchSettingsMap);
        dispose();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        panel2 = new JPanel();
        label1 = new JLabel();
        usernameText = new JTextField();
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
        closeButton = new JButton();
        addButton = new JButton();

        //======== this ========
        setTitle("Add Account");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "default",
            "default:grow"));

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(250, 300));
            panel1.setLayout(new FormLayout(
                "default:grow",
                "default, $lgap, default"));

            //======== panel2 ========
            {
                panel2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.setLayout(new FormLayout(
                    "default, $lcgap, 68dlu",
                    "6*(default, $lgap), default"));

                //---- label1 ----
                label1.setText("Username:");
                label1.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label1, CC.xy(1, 1, CC.RIGHT, CC.DEFAULT));

                //---- usernameText ----
                usernameText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                usernameText.setName("username");
                panel2.add(usernameText, CC.xy(3, 1));

                //---- label2 ----
                label2.setText("Password:");
                label2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label2, CC.xy(1, 3, CC.RIGHT, CC.DEFAULT));

                //---- passwordText ----
                passwordText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                passwordText.setName("password");
                panel2.add(passwordText, CC.xy(3, 3));

                //---- label3 ----
                label3.setText("Zip:");
                label3.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label3, CC.xy(1, 5, CC.RIGHT, CC.DEFAULT));

                //---- zipText ----
                zipText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                zipText.setName("zip");
                panel2.add(zipText, CC.xy(3, 5));

                //---- label4 ----
                label4.setText("Proxy:");
                label4.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label4, CC.xy(1, 7, CC.RIGHT, CC.DEFAULT));

                //---- proxyText ----
                proxyText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                proxyText.setName("proxy");
                panel2.add(proxyText, CC.xy(3, 7));

                //---- label5 ----
                label5.setText("Port:");
                label5.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label5, CC.xy(1, 9, CC.RIGHT, CC.DEFAULT));

                //---- portText ----
                portText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                portText.setName("port");
                panel2.add(portText, CC.xy(3, 9));

                //---- label6 ----
                label6.setText("Proxy username:");
                label6.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label6, CC.xy(1, 11, CC.RIGHT, CC.DEFAULT));

                //---- proxyUsernameText ----
                proxyUsernameText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                proxyUsernameText.setName("proxyUsername");
                panel2.add(proxyUsernameText, CC.xy(3, 11));

                //---- label7 ----
                label7.setText("Proxy password:");
                label7.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                panel2.add(label7, CC.xy(1, 13, CC.RIGHT, CC.DEFAULT));

                //---- proxyPasswordText ----
                proxyPasswordText.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
                proxyPasswordText.setName("proxyPassword");
                panel2.add(proxyPasswordText, CC.xy(3, 13));
            }
            panel1.add(panel2, CC.xy(1, 1, CC.DEFAULT, CC.TOP));

            //======== buttonPanel ========
            {
                buttonPanel.setLayout(new FormLayout(
                    "default, $lcgap, default",
                    "default"));

                //---- closeButton ----
                closeButton.setText("Close");
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        closeButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(closeButton, CC.xy(1, 1, CC.CENTER, CC.DEFAULT));

                //---- addButton ----
                addButton.setText("Add");
                addButton.setEnabled(false);
                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(addButton, CC.xy(3, 1));
            }
            panel1.add(buttonPanel, new CellConstraints(1, 3, 1, 1, CC.RIGHT, CC.DEFAULT, new Insets(0, 0, 0, 10)));
        }
        contentPane.add(panel1, new CellConstraints(1, 1, 1, 1, CC.DEFAULT, CC.DEFAULT, new Insets(10, 10, 0, 0)));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JPanel panel2;
    private JLabel label1;
    private JTextField usernameText;
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
    private JButton closeButton;
    private JButton addButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
