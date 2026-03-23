package pr.sambogram;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Controller {
    @FXML private TextArea logArea;
    @FXML private TextField messageField;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private Button sendButton;
    @FXML private Button settingsButton;

    private Socket socket;
    private PrintWriter out;
    private String username = "User";
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        logArea.textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> logArea.setScrollTop(Double.MAX_VALUE));
        });

        try {
            ImageView iconView = new ImageView(new Image(getClass().getResourceAsStream("LOGO.png")));
            iconView.setFitHeight(18);
            iconView.setFitWidth(18);
            settingsButton.setGraphic(iconView);
            settingsButton.setText("");
        } catch (Exception e) { }

        Platform.runLater(this::showConnectDialog);
    }

    @FXML
    protected void showConnectDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Sambogram Login");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("LOGO.png")));
            logoView.setFitHeight(70);
            logoView.setFitWidth(70);
            content.getChildren().add(logoView);
        } catch (Exception e) { }

        Label title = new Label("Sambogram Hub");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setAlignment(Pos.CENTER);

        TextField ip = new TextField("localhost");
        TextField port = new TextField("8080");
        TextField name = new TextField(username);

        grid.add(new Label("IP:"), 0, 0); grid.add(ip, 1, 0);
        grid.add(new Label("Port:"), 0, 1); grid.add(port, 1, 1);
        grid.add(new Label("Name:"), 0, 2); grid.add(name, 1, 2);

        content.getChildren().addAll(title, grid);
        dialogPane.setContent(content);

        ButtonType loginBtn = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == loginBtn ? new String[]{ip.getText(), port.getText(), name.getText()} : null);

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(data -> {
            this.username = data[2];
            userLabel.setText("Active: " + username);
            connectToServer(data[0], Integer.parseInt(data[1]));
        });
    }

    private void connectToServer(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                Platform.runLater(() -> {
                    statusLabel.setText("Online");
                    statusLabel.setStyle("-fx-text-fill: #3842EF; -fx-font-weight: bold;");
                });

                String msg;
                while ((msg = in.readLine()) != null) {
                    String finalMsg = msg;
                    Platform.runLater(() -> logArea.appendText(finalMsg + "\n"));
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Offline");
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                });
            }
        }).start();
    }

    @FXML
    protected void onSendButtonClick() {
        if (out != null && !messageField.getText().isEmpty()) {
            String time = LocalTime.now().format(timeFormatter);
            out.println("[" + time + "] " + username + ": " + messageField.getText());
            messageField.clear();
        }
    }

    @FXML protected void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) onSendButtonClick();
    }
}