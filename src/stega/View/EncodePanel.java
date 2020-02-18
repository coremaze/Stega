package stega.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
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

public class EncodePanel extends JPanel {

    Controller controller;

    JPanel leftPanel;
    JPanel inputPanel;
    JPanel outputPanel;
    JPanel encodePanel;

    JPanel rightPanel;
    JPanel buttonsPanel;

    JTextField input;
    JTextField output;

    DefaultListModel<String> listModel;

    JButton encodeButton;
    JButton addButton;
    JButton removeButton;

    JButton inputSelectButton;

    JList<String> fileList;
    JScrollPane scrollPane;

    public EncodePanel(Controller controller) {
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
        encodePanel = new JPanel();

        add(leftPanel, BorderLayout.WEST);
        leftPanel.add(inputPanel);
        leftPanel.add(outputPanel);
        leftPanel.add(encodePanel);

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

        encodeButton = new JButton("Encode");
        encodeButton.addActionListener((ActionEvent evt) -> {
            encodeClicked();
        });
        encodePanel.add(encodeButton);

        listModel = new DefaultListModel<>();

        fileList = new JList<>(listModel);
        scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(10, 300));
        rightPanel.add(scrollPane, BorderLayout.NORTH);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);

        addButton = new JButton("+");
        addButton.addActionListener((ActionEvent evt) -> {
            addClicked();
        });

        buttonsPanel.add(addButton);
        removeButton = new JButton("-");
        removeButton.addActionListener((ActionEvent evt) -> {
            removeClicked();
        });
        buttonsPanel.add(removeButton);
    }

    private void addClicked() {
        FileDialog dialog = new FileDialog((Frame) null, "Select File");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        this.controller.addFile(dialog.getDirectory(), dialog.getFile());
    }

    private void removeClicked() {
        for (String fileName : this.fileList.getSelectedValuesList()) {
            this.controller.removeFile(fileName);
        }
    }

    private void encodeClicked() {
        controller.encodeAndSave(input.getText(), output.getText());
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
            input.setText(filePath);
            output.setText(filePath);
        }
    }
}
