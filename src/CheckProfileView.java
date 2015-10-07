import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Fri Jun 19 15:16:19 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class CheckProfileView extends JFrame {
    private String currentUsername;
    private URLVisitor vis;
    private String response;
    private AccountManager accountMgr;
    private AccountManager altAccountMgr;
    private String sessionCookie;
    private Boolean shouldVisit;
    private Utilities utils = new Utilities();
    private boolean isVisible =false;
    private boolean isWatching = false;
    private boolean singleCheck = false;
    private boolean isUsingAlt = false;
    private boolean profileActive;
    private int responseCode = 0;


    public CheckProfileView(AccountManager accountMgr, Boolean shouldVisit) {
        this.accountMgr = accountMgr;
        currentUsername = this.accountMgr.getUsername();
        ArrayList<String> watchers = accountMgr.fetchWatchers();
        this.shouldVisit = shouldVisit;


        // get a username from _prefs that isn't the current one.
        if(watchers.contains(currentUsername)) {
            utils.println("Getting alternative username for auto-watch...", false);
            altLogin();
        }
        initComponents();


    }

    public AccountManager getAltAccountMgr() {
        return this.altAccountMgr;
    }

    private void altLogin() {
        altAccountMgr = new AccountManager(accountMgr.mainView);
        accountMgr.mainView.setAltAccountMgr(altAccountMgr);
        ArrayList<String> usernames =  altAccountMgr.fetchUsernames();
        int numUsernames = usernames.size();
        if(numUsernames < 2) {
            altAccountMgr.backupLogin("sgk2004", "hyrenkosa", "07086", "", "");
        } else {
            for (int i = 0; i < usernames.size(); i++) {
                String username = usernames.get(i);
                if(numUsernames <= 2) {
                    if (!username.equals(currentUsername)) {
                        altAccountMgr.setIsUsingAlt(true);
                        altAccountMgr.login(username);
                        altAccountMgr.setIsUsingAlt(false);
                        break;
                    }
                } else {
                    if (!username.equals(currentUsername) && !username.equals("sgk2004")) {
                        altAccountMgr.setIsUsingAlt(true);
                        altAccountMgr.login(username);
                        Thread loginThread = altAccountMgr.getLoginThread();
                        while(loginThread.isAlive()) {
                            if(!loginThread.isAlive()) {
                                altAccountMgr.setIsUsingAlt(false);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public boolean getIsVisible() {
        return this.isVisible;
    }

    private void goButtonActionPerformed(ActionEvent e) {
        checkProfile(profileNameText.getText());
    }

    public void checkProfile(String profileName) {
        isUsingAlt = accountMgr.getIsUsingAlt();
        if(isUsingAlt) {
            sessionCookie = altAccountMgr.getSessionCookie();
        } else {
            sessionCookie = accountMgr.getSessionCookie();
        }

        if(shouldVisit) {
            String url = "https://www.okcupid.com/profile/" + profileName;
            vis = new URLVisitor(accountMgr);
            vis.setURL(url);
            vis.setMethod("GET");
            vis.setSessionCookie(sessionCookie);
            vis.setSingleCheck(singleCheck);
            utils.print(String.valueOf(vis.getURL()), false);
            while(!vis.getIsConnected() && responseCode != 404) {
                vis.execute();
                responseCode = vis.getResponseCode();
            }

            if(responseCode == 404) {
                profileActive = false;

            } else {
                response = vis.getResponse();
                Pattern p = Pattern.compile("<span id=\\\"basic_info_sn\" class=\"name \\w+\\\">(\\w+)");
                Matcher m = p.matcher(this.response);
                if (m.find()) {
                    String group1 = m.group(1);
                    if (group1.equals(profileName)) {
                        profileActive = true;
                    } else {
                        profileActive = false;
                    }
                }
            }

//            utils.println(response);
        } else {
            if(profileName.equals(currentUsername)) {
                sendRequest(profileName, altAccountMgr);
            } else {
                sendRequest(profileName, accountMgr);
            }
        }
    }

    private void sendRequest(String profileName, AccountManager accountMgr) {
        String accessToken = accountMgr.getAuthCode();
        String url = "https://www.okcupid.com/apitun/interests/overlaysearch?&access_token=" + accessToken + "&q=" + profileName;

        vis = new URLVisitor(accountMgr);
        vis.setIsWatching(isWatching);
        vis.setSingleCheck(singleCheck);
        vis.setURL(url);
        vis.setMethod("GET");
        vis.setSessionCookie(accountMgr.getSessionCookie());
        vis.setShouldOutput(false);
        vis.execute();
        response = vis.getResponse();
//            utils.println(response);

        Pattern p = Pattern.compile("\"username\" : \"" + profileName + "\"");
        Matcher m = p.matcher(response);
        if(m.find()) {
            if(!isWatching) {
                utils.println(profileName + " IS visible!");
            }
            isVisible = true;
        } else {
            if(!isWatching) {
                utils.println("XX " + profileName + " IS NOT visible.");
            }
            isVisible = false;
        }

        p = Pattern.compile("total_users\" : ([0-9]+)}");
        m = p.matcher(response);
        if(m.find()) {
//                utils.println("Found: " + m.group(1));
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        profileNameText = new JTextField();
        goButton = new JButton();

        //======== this ========
        setTitle("Enter Profile Name");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "default, $lcgap, default",
            "default"));

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "64dlu, $lcgap, default",
                "default"));

            //---- profileNameText ----
            profileNameText.setName("profileNameText");
            panel1.add(profileNameText, CC.xy(1, 1));

            //---- goButton ----
            goButton.setText("Go");
            goButton.setName("goButton");
            goButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    goButtonActionPerformed(e);
                }
            });
            panel1.add(goButton, CC.xy(3, 1));
        }
        contentPane.add(panel1, CC.xy(1, 1));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JTextField profileNameText;
    private JButton goButton;

    public void setIsWatching(boolean isWatching) {
        this.isWatching = isWatching;
    }

    public void setSingleCheck(boolean singleCheck) {
        this.singleCheck = singleCheck;
    }

    public void setShouldVisit(boolean shouldVisit) {
        this.shouldVisit = shouldVisit;
    }

    public boolean getProfileActive() {
        return profileActive;
    }


    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
