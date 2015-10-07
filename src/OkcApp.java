import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.GraphicAttribute;
import java.lang.management.MonitorInfo;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Created by stuartkuredjian on 5/19/15.
 */

public class OkcApp extends JFrame {
    private static MainView mainView = new MainView();
    private JPanel panel1 = mainView.getPanel1();
    private static ArrayList outputArray = new ArrayList();
    private Utilities utils = new Utilities(this);
    private static JTextArea textArea1 = mainView.getConsoleView().getTextArea1();
    static DefaultCaret caret = (DefaultCaret) textArea1.getCaret();

    public static void main(String[] args) {
        new OkcApp();
    }

    public static synchronized void printToUIConsole() {
        initPrintThread();
    }

    public OkcApp() {
        final Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("UI Thread");
                setContentPane(panel1);
                setLocation();
                setResizable(false);
                setVisible(true);
                pack();

                addWindowFocusListener(new WindowAdapter() {
                    @Override
                    public void windowGainedFocus(WindowEvent windowEvent) {
                        int oldSelectedIndex = 0;
                        if (getContentPane().isVisible()) {
                            if (mainView.getUsernamesCombo().isVisible()) {
                                oldSelectedIndex = mainView.usernamesCombo.getSelectedIndex();
                            }
                        } else {
                            oldSelectedIndex = mainView.usernamesCombo.getSelectedIndex();
                        }
                        mainView.accountMgr.populateUsernames(mainView.usernamesCombo);
                        mainView.usernamesCombo.setSelectedItem(oldSelectedIndex);
                        Boolean isLoggedIn = mainView.accountMgr.getIsLoggedIn();
                        if (isLoggedIn) {
                            mainView.populateSettings(mainView.accountMgr.fetchSearchSettings(mainView.accountMgr.getUsername()));
                        }
                        try {
                            if (mainView.usernamesCombo.isVisible() && !mainView.accountMgr.getIsLoggedIn()) {
                                mainView.clearAccountsMenuItem.setEnabled(true);
                                mainView.usernamesCombo.setSelectedIndex(oldSelectedIndex);
                            } else {
                                mainView.clearAccountsMenuItem.setEnabled(false);
                            }
                        } catch (Exception e) {
                            utils.println("Exception");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        uiThread.start();
    }

    private static void initPrintThread() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Print Thread");
                if (!outputArray.isEmpty()) {
                    for (int i = 0; i < outputArray.size(); i++) {
                        String line = String.valueOf(outputArray.get(i));
                        appendTextArea(line);
                    }
                    outputArray.clear();
                }
            }
        });
    }

    private static void appendTextArea(String line) {
        textArea1.append(line);
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    public static ArrayList getOutputArray() {
        return outputArray;
    }

    private void setLocation() {
        Rectangle bounds = new Rectangle();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] listGD = env.getScreenDevices();
        for(GraphicsDevice screen : listGD) {
            bounds.add(screen.getDefaultConfiguration().getBounds());
        }

        Point pos = MouseInfo.getPointerInfo().getLocation();
        int launchPos = 0;
        if(pos.getX() > bounds.getWidth()/2) {
            launchPos = (int) bounds.getWidth()/2+10;
        } else {
            launchPos = (int) listGD[0].getDefaultConfiguration().getBounds().getX()-(panel1.getWidth()+10);
        }
        setLocation(launchPos, 0);
    }
}
