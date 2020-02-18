package stega.Model;

public class FileInfo {

    private final String path;
    private final String fileName;

    public FileInfo(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public String getPath() {
        return this.path;
    }

    public String getFileName() {
        return this.fileName;
    }
}
