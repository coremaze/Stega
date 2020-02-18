package stega.Controller;

import java.io.IOException;
import java.util.TreeMap;
import stega.Model.Model;
import stega.View.View;

public class Controller {

    private final Model model;
    private final View view;

    public Controller() {
        this.model = new Model(this);
        this.view = new View(this);
    }

    public void addFile(String filePath, String fileName) {
        if (this.model.addFile(filePath, fileName)) {
            warn(String.format("There is already a file called %s", fileName));
        }
        this.view.updateSelectedFiles(model.getFileNames());
    }

    public void removeFile(String fileName) {
        this.model.removeFile(fileName);
        this.view.updateSelectedFiles(model.getFileNames());
    }

    public TreeMap<String, byte[]> getFilesFromImage(String imageFilePath) {
        try {
            return this.model.getFilesFromImage(imageFilePath);
        } catch (IllegalArgumentException exc) {
            warn(exc.getMessage());
        }
        return new TreeMap<String, byte[]>();

    }

    public void saveFile(String fileName, TreeMap<String, byte[]> map, String path) {
        byte[] contents;
        if ((contents = map.get(fileName)) != null) {
            this.model.saveFile(path, fileName, contents);
        }
    }

    public void encodeAndSave(String input, String output) {
        if (!output.endsWith(".png")) {
            warn("Sorry! Will only create PNG files!");
            return;
        }
        try {
            byte[] data = model.encodeFiles();
            model.encodeBufferIntoImage(input, output, data);
        } catch (IOException ex) {
            warn(ex.getMessage());
        } catch (java.lang.IllegalArgumentException exc) {
            warn(exc.getMessage());
        }
    }
    
    public void warn(String warning) {
        view.getMainFrame().showDialog(warning);
    }
}
