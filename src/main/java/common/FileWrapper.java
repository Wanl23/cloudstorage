package common;

import java.io.File;

public class FileWrapper extends AbstractMessage {
    File file;

    public FileWrapper(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
