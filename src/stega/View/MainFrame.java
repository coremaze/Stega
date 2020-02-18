package stega.View;

import java.awt.Frame;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import stega.Controller.Controller;

public class MainFrame extends JFrame {

    private JTabbedPane tabs;
    private EncodePanel encodePanel;
    private DecodePanel decodePanel;
    private final Controller controller;

    public MainFrame(Controller controller) throws HeadlessException {
        this.controller = controller;
        this.initialize();
    }

    private void initialize() {
        this.tabs = new JTabbedPane();
        encodePanel = new EncodePanel(controller);
        tabs.addTab("Encode", encodePanel);
        decodePanel = new DecodePanel(controller);
        tabs.addTab("Decode", decodePanel);
        this.setContentPane(tabs);
        this.setTitle("Stega");
        this.setSize(700, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);
    }

    public EncodePanel getEncodePanel() {
        return this.encodePanel;
    }

    public void showDialog(String str) {
        JOptionPane.showMessageDialog((Frame) null, str);
    }

}
