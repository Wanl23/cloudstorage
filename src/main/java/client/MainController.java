package client;

import common.AbstractMessage;
import common.FileMessage;
import common.FileRequest;
import common.FileWrapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> clientFilesList;

    @FXML
    ListView<String> cloudFileList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
    }

    public void onDropped(DragEvent dragEvent) {
        File file = dragEvent.getDragboard().getFiles().stream().findFirst().get();
        FileWrapper fileWrapper = new FileWrapper(file);
        Network.sendMsg(fileWrapper);
        refreshLocalFilesList();
    }

    public void onClicked(MouseEvent arg0) throws IOException {
        FileRequest fileRequest = new FileRequest(cloudFileList.getSelectionModel().getSelectedItem());
        Network.sendMsg(fileRequest);
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
                cloudFileList.getItems().clear();
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> cloudFileList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    clientFilesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
                    cloudFileList.getItems().clear();
                    Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> cloudFileList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
