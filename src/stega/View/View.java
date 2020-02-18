package stega.View;

import java.util.ArrayList;
import stega.Controller.Controller;

public class View {

    private final MainFrame mainFrame;

    public View(Controller controller) {
        this.mainFrame = new MainFrame(controller);
    }

    public void updateSelectedFiles(ArrayList<String> fileNames) {
        this.mainFrame.getEncodePanel().updateSelectedFiles(fileNames);
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }
}
