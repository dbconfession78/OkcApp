import java.util.ArrayList;

/**
 * Created by stuartkuredjian on 2/12/15.
 */
public class Logger {
    private static MainView mainView;
    private ArrayList outputArray = new ArrayList();

    public Logger(MainView mainView) {
        this.mainView = mainView;
    }

    public void setOutput(String s) {
        outputArray.add(s);
    }

    public ArrayList getOutputArray() {
        return this.outputArray;
    }
}