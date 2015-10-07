import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import javax.swing.*;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stuartkuredjian on 6/4/15.
 */
public class Utilities {
    private boolean shouldPrint = true;
    ArrayList outputArray;
    private HashMap<String, String> userInputMap;
    private OkcApp okcApp;

    public Utilities(OkcApp okcApp) {
        this.okcApp = okcApp;
        outputArray = OkcApp.getOutputArray();
    }

    public Utilities() {
        outputArray = OkcApp.getOutputArray();
    }

    public void notify(String[] recipients, String subject, String message) {
        try {
            new NotificationHandler(recipients, subject, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findString(String body, String targetString, int group) {
        String result = "";
        Pattern p = Pattern.compile(targetString);
        Matcher m = p.matcher(body);
        if(m.find()) {
            try {
                result = m.group(group);
            } catch (IndexOutOfBoundsException e) {
                println("Error: Please specify group " + group + " in " + targetString + "by surrounding it with ( )." , false);
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                Thread.currentThread().stop();
            }
        } else {
            result = null;
        }

        return result;
    }

    public HashMap<String, String> generateUserInputMap(JPanel jPanel, HashMap hashMap) {
        int componentCount = jPanel.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            Component component = jPanel.getComponent(i);
            String componentClassName = component.getClass().getName();
            String componentName = component.getName();

            if(componentClassName.endsWith("JPanel")) {
                JPanel jPanel2 = (JPanel) component;
                generateUserInputMap(jPanel2, hashMap);
                continue;
            }

            if(componentClassName.endsWith("JTextField") || componentClassName.endsWith("JComboBox") || componentClassName.endsWith("JCheckBox")) {
                hashMap.put(componentName, "");
                continue;
            }
        }

        return hashMap;
    }

    public static void publishMap(HashMap<String, String> userInputMap, Preferences _prefs) {
        Iterator iterator = userInputMap.keySet().iterator();
        while(iterator.hasNext()) {
            String next = String.valueOf(iterator.next());
            _prefs.put(next, userInputMap.get(next));
        }
    }

    public String findText(String string, String a, String d) {
        // finds string between a and d
        String[] b = string.split(a);
        String[] c = b[1].split(d);
        return c[0];
    }

    public void populateMap(JPanel jPanel, HashMap<String, String> userInputMap) {
        for (int i = 0; i < jPanel.getComponentCount(); i++) {
            Component component = jPanel.getComponent(i);
            String componentClassName = component.getClass().getCanonicalName();
            String componentName = component.getName();
            String key = componentName;
            String value = "";

            if(componentClassName.endsWith("JPanel")) {
                JPanel jPanel2 = (JPanel) component;
                populateMap(jPanel2, userInputMap);
                continue;
            }

            if(userInputMap.containsKey(key)) {
                if(componentClassName.endsWith("JTextField")) {
                    JTextField jTextField = (JTextField) component;
                    if(!componentName.contains("assword")) {
                        value = jTextField.getText().toLowerCase();
                    } else {
                        value = jTextField.getText();
                    }

                    userInputMap.put(key, value);
                    continue;
                }

                if(componentClassName.endsWith("JCheckBox")) {
                    JCheckBox jCheckBox= (JCheckBox) component;
                    key = jCheckBox.getName();
                    value = String.valueOf(jCheckBox.isSelected());
                    userInputMap.put(key, value);
                    continue;
                }

                if(componentClassName.endsWith("JComboBox")) {
                    JComboBox jComboBox= (JComboBox) component;
                    userInputMap.put(componentName, String.valueOf(jComboBox.getSelectedItem()).toLowerCase());
                    continue;
                }
            }
        }
    }

    public void resetComponents(JPanel jPanel) {
        Component[] components = jPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            String componentClassName = components[i].getClass().getName();
            if(componentClassName.equals("javax.swing.JTextField")) {
                JTextField jTextField = (JTextField) components[i];
                jTextField.setText("");
            }

            if(componentClassName.equals("javax.swing.JComboBox")) {
                JComboBox jComboBox= (JComboBox) components[i];
                if(jComboBox.getItemCount() > 0) {
                    jComboBox.setSelectedIndex(0);
                }

            }

            if(componentClassName.equals("javax.swing.JCheckBox")) {
                JCheckBox jCheckBox= (JCheckBox) components[i];
                jCheckBox.setSelected(false);
            }

            if(componentClassName.equals("javax.swing.JPanel")) {
                JPanel jPanel1= (JPanel) components[i];
                resetComponents(jPanel1);
            }
        }
    }

    public void populateComponents(JPanel jPanel, HashMap<String, String> userInputMap) {
        for (int i = 0; i < jPanel.getComponentCount(); i++) {
            Component component = jPanel.getComponent(i);
            String componentClassName = component.getClass().getName();
            String componentName = component.getName();

            if(componentClassName.endsWith("JPanel")) {
                JPanel jPanel2 = (JPanel) component;
                populateComponents(jPanel2, userInputMap);
                continue;
            }

            if(userInputMap.containsKey(componentName)) {
                if(componentClassName.endsWith("JTextField")) {
                    JTextField jTextField = (JTextField) component;
                    String textValue = userInputMap.get(componentName);
                    jTextField.setText(textValue);
                    continue;
                }

                if(componentClassName.endsWith("JCheckBox")) {
                    JCheckBox jCheckBox = (JCheckBox) component;
                    jCheckBox.setSelected(Boolean.parseBoolean(userInputMap.get(componentName)));
                    continue;
                }

                if(componentClassName.endsWith("JComboBox")) {
                    JComboBox jComboBox= (JComboBox) component;
                    String firstItem = String.valueOf(jComboBox.getItemAt(0));

                    if(Character.isUpperCase(firstItem.charAt(0))) {
                        jComboBox.setSelectedItem(
                                    (userInputMap.get(componentName).substring(0,1).toUpperCase()) +
                                        (userInputMap.get(componentName).substring(1))
                        );
                    } else {
                        jComboBox.setSelectedItem(userInputMap.get(componentName));
                    }
                    continue;
                }
            }
        }
    }

    public void statusLine(String output) {
        int parsePosition = 0;
        JLabel statusLabel = MainView.getConsoleOutputLabel();

        if (output.contains("AM")) {
            parsePosition = output.indexOf("AM: ");
        }
        if (output.contains("PM")) {
            parsePosition = output.indexOf("PM: ");
        }
        output = output.substring(parsePosition + 4);
        statusLabel.setText(output);
    }

    public void println(String string) {
        println2(string, false, true);
    }

    public void println(String string, boolean outputUI) {
        println2(string, outputUI, true);
    }

    public void println(String string, boolean outputUI, boolean shouldTimeStamp) {
        println2(string, outputUI, shouldTimeStamp);
    }

    public void println2(String string, boolean outputUI, boolean shouldTimeStamp) {
        outputArray = OkcApp.getOutputArray();
        String output = "";
        if(shouldTimeStamp) {
            if (string.startsWith("\n")) {
                string = string.replaceFirst("\n", "");
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                Date date = new Date();
                output = "\n" + dateFormat.format(date) + ":  " + string;
            } else {
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                Date date = new Date();
                output = dateFormat.format(date) + ":  " + string;
            }
        } else {
            output = string;
        }
        System.out.println(output);
        if(outputUI == true) {
            statusLine(output);
        }
        outputArray.add(output + "\n");
        OkcApp.printToUIConsole();

    }

    public void print(String string) {
        print2(string, false, true);
    }

    public void print(String string, boolean outputUI) {
            print2(string, outputUI, true);
    }

    public void print(String string, boolean outputUI, boolean shouldTimeStamp) {
        print2(string, outputUI, shouldTimeStamp);
    }

    public void print2(String string, boolean outputUI, boolean shouldTimeStamp) {
        outputArray = OkcApp.getOutputArray();
        String output = "";
        if(shouldTimeStamp) {
            if (string.startsWith("\n")) {
                string = string.replaceFirst("\n", "");
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                Date date = new Date();
                output = "\n" + dateFormat.format(date) + ":  " + string;
            } else {
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                Date date = new Date();
                output = dateFormat.format(date) + ":  " + string;
            }
        } else {
            output = string;
        }
        System.out.print(output);
        if(outputUI == true) {
            statusLine(output);
        }
        outputArray.add(output);
        OkcApp.printToUIConsole();
    }

    public void killAllThreads(Boolean isLoggedIn) {
        Map threadMap = Thread.getAllStackTraces();
        Iterator iterator = threadMap.keySet().iterator();
        while(iterator.hasNext()) {
            Thread thread = (Thread) iterator.next();
            String threadName = thread.getName();
            if(!isLoggedIn) {
                if(threadName.startsWith("AutoWatchThread-")) {
                    thread.stop();
                }

                if(threadName.startsWith("Thread-")) {
                    thread.stop();
                }
            }


            if(threadName.startsWith("Visitor Count Thread")) {
                thread.stop();
            }
            if(threadName.startsWith("Next Visit Thread")) {
                thread.stop();
            }
            if(threadName.startsWith("Run Thread")) {
                thread.stop();
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                println("\nRun Terminated", true);
            }


        }
    }

    public void setShouldPrint(boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }

    public void stopThread(Thread thread) {
        println("Stopping thread(s): " + thread, false);
        thread.stop();
    }

    public void stopThread(String threadName) {
        println("Stopping thread(s): " + threadName, false);
        Map allStackTraces = Thread.getAllStackTraces();
        Iterator iterator = allStackTraces.keySet().iterator();
        while(iterator.hasNext()) {
            Thread t = (Thread) iterator.next();
            String tName = t.getName();
            if(tName.startsWith(threadName)) {
                t.stop();
            }
        }
    }

    public Thread getThread(String threadName) {
        Thread thread;
        Map threads = Thread.getAllStackTraces();
        Iterator iterator = threads.keySet().iterator();
        while(iterator.hasNext()) {
            thread = (Thread) iterator.next();
            String threadName2 = thread.getName();
            System.out.println("threadName2:" + threadName2);
            if(threadName2.equals(threadName)) {
                return thread;
            }
        }
        this.println("\"" + threadName + "\" does not exist");
        return null;
    }

    public void killAllThreadOccurances(Thread thread) {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Iterator iterator = threads.keySet().iterator();
        while(iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            String value = String.valueOf(threads.get(key));

            if(key.equals(thread)) {
                thread.stop();
            }
        }
        if(thread.equals(thread)) {
            thread.stop();
        }
    }

    public HashMap addUnderscores(HashMap hashMap) {
        Iterator iterator = hashMap.keySet().iterator();
        while(iterator.hasNext()) {
            String mapKey = String.valueOf(iterator.next());
            String originalMapKey = mapKey;
            String mapValue = String.valueOf(hashMap.get(mapKey));
            String originalMapValue = mapValue;
            mapKey = mapKey.replaceAll(" ", "_");
            mapKey = mapKey.replaceAll("-", "_");
            mapKey = mapKey.replaceAll("/", "_");

            mapValue = mapValue.replaceAll(" ", "_");
            mapValue = mapValue.replaceAll("-", "_");
            mapValue = mapValue.replaceAll("/", "_");

            if(!mapKey.equals(originalMapKey) || !mapValue.equals(originalMapValue)) {
                hashMap.remove(originalMapKey);
                hashMap.put(mapKey, mapValue);
                iterator = hashMap.keySet().iterator();
            }
        }
        return hashMap;
    }
}