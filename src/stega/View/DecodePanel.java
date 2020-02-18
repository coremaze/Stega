package stega.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import stega.Controller.Controller;

public class DecodePanel extends JPanel {

    Controller controller;

    JPanel leftPanel;
    JPanel inputPanel;
    JPanel outputPanel;
    JPanel decodePanel;

    JPanel rightPanel;
    JPanel buttonsPanel;

    JTextField input;
    JTextField output;

    DefaultListModel<String> listModel;

    JButton decodeButton;
    JButton saveButton;

    JButton inputSelectButton;
    JButton outputSelectButton;

    JList<String> fileNameList;
    TreeMap<String, byte[]> fileDataMap;
    JScrollPane scrollPane;

    public DecodePanel(Controller controller) {
        this.controller = controller;
        this.initialize();
    }

    private void initialize() {
        setLayout(new GridLayout());

        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        outputPanel = new JPanel();
        outputPanel.setLayout(new GridBagLayout());
        decodePanel = new JPanel();

        add(leftPanel, BorderLayout.WEST);
        leftPanel.add(inputPanel);
        leftPanel.add(outputPanel);
        leftPanel.add(decodePanel);

        add(rightPanel, BorderLayout.EAST);

        input = new JTextField(20);
        inputPanel.add(new JLabel("Input: "));
        inputPanel.add(input);

        inputSelectButton = new JButton("+");
        inputSelectButton.addActionListener((ActionEvent evt) -> {
            inputSelectClicked();
        });
        inputPanel.add(inputSelectButton);

        output = new JTextField(20);
        outputPanel.add(new JLabel("Output: "));
        outputPanel.add(output);

        outputSelectButton = new JButton("+");
        outputSelectButton.addActionListener((ActionEvent evt) -> {
            outputSelectClicked();
        });
        outputPanel.add(outputSelectButton);

        decodeButton = new JButton("Decode");
        decodeButton.addActionListener((ActionEvent evt) -> {
            decodeClicked();
        });
        decodePanel.add(decodeButton);

        listModel = new DefaultListModel<>();

        fileNameList = new JList<>(listModel);
        scrollPane = new JScrollPane(fileNameList);
        scrollPane.setPreferredSize(new Dimension(10, 300));
        rightPanel.add(scrollPane, BorderLayout.NORTH);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);

        saveButton = new JButton("Save");
        saveButton.addActionListener((ActionEvent evt) -> {
            saveClicked();
        });

        buttonsPanel.add(saveButton);

    }

    private void saveClicked() {
        for (String fileName : this.fileNameList.getSelectedValuesList()) {
            this.controller.saveFile(fileName, fileDataMap, output.getText());
        }
    }

    private void decodeClicked() {
        fileDataMap = this.controller.getFilesFromImage(input.getText());
        ArrayList<String> newFileNames = new ArrayList<>();
        for (String fileName : fileDataMap.keySet()) {
            newFileNames.add(fileName);
        }
        updateSelectedFiles(newFileNames);
    }

    public void updateSelectedFiles(ArrayList<String> fileNames) {
        this.listModel.clear();
        for (String name : fileNames) {
            this.listModel.addElement(name);
        }
    }

    private void inputSelectClicked() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PNG Images (*.png)", "png"));
        fc.setVisible(true);
        fc.showOpenDialog(rightPanel);
        File file = fc.getSelectedFile();
        if (file != null) {
            String filePath = file.getPath();
            String fileDirectory = file.getParent();
            input.setText(filePath);
            output.setText(fileDirectory);
        }
    }

    private void outputSelectClicked() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setVisible(true);
        fc.showOpenDialog(rightPanel);
        File file = fc.getSelectedFile();
        if (file != null) {
            String filePath = file.getPath();
            output.setText(filePath);
        }
    }
}
