import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Mon Aug 10 21:19:57 EDT 2015
 */



/**
 * @author Stuart Kuredjian
 */
public class ConsoleView extends JFrame {
    MainView mainView;

    public ConsoleView(MainView mainView) {
        initComponents();
        this.mainView = mainView;
    }

    public JPanel getPanel1() {
        return this.panel1;
    }

    public JTextArea getTextArea1() {
        return textArea1;
    }

    private void closeButtonActionPerformed(ActionEvent e) {
        mainView.toggleConsole();
    }

    private void clearButtonActionPerformed(ActionEvent e) {
        textArea1.setText("");
    }



    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel1 = new JPanel();
        scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();
        buttonPanel = new JPanel();
        clearButton = new JButton();
        button1 = new JButton();
        closeButton = new JButton();

        //======== this ========
        setResizable(false);
        setFocusable(false);
        setFocusableWindowState(false);
        setTitle("Console");
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "default:grow",
            "fill:default:grow"));

        //======== panel1 ========
        {
            panel1.setPreferredSize(new Dimension(800, 400));
            panel1.setFocusable(false);
            panel1.setRequestFocusEnabled(false);
            panel1.setVerifyInputWhenFocusTarget(false);
            panel1.setLayout(new FormLayout(
                "default:grow",
                "fill:default:grow, default"));

            //======== scrollPane1 ========
            {
                scrollPane1.setAutoscrolls(true);
                scrollPane1.setFocusable(false);
                scrollPane1.setRequestFocusEnabled(false);
                scrollPane1.setVerifyInputWhenFocusTarget(false);

                //---- textArea1 ----
                textArea1.setRequestFocusEnabled(false);
                textArea1.setVerifyInputWhenFocusTarget(false);
                textArea1.setEditable(false);
                scrollPane1.setViewportView(textArea1);
            }
            panel1.add(scrollPane1, CC.xy(1, 1));

            //======== buttonPanel ========
            {
                buttonPanel.setLayout(new FormLayout(
                    "center:default, $lcgap, right:default:grow, $lcgap, default",
                    "default"));

                //---- clearButton ----
                clearButton.setText("Clear");
                clearButton.setName("clearButton");
                clearButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clearButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(clearButton, CC.xy(1, 1));

                //---- button1 ----
                button1.setText("Save as...");
                buttonPanel.add(button1, CC.xy(3, 1));

                //---- closeButton ----
                closeButton.setText("Close");
                closeButton.setName("closeButton");
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        closeButtonActionPerformed(e);
                    }
                });
                buttonPanel.add(closeButton, CC.xy(5, 1, CC.RIGHT, CC.DEFAULT));
            }
            panel1.add(buttonPanel, CC.xy(1, 2, CC.FILL, CC.DEFAULT));
        }
        contentPane.add(panel1, CC.xy(1, 1));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTextArea textArea1;
    private JPanel buttonPanel;
    private JButton clearButton;
    private JButton button1;
    private JButton closeButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
