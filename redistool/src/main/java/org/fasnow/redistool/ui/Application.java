package org.fasnow.redistool.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fasnow.redistool.HttpClient;
import org.fasnow.redistool.Proxy;
import org.fasnow.redistool.Utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Application extends javafx.application.Application {
    private static final Proxy proxy = new Proxy("127.0.0.1",8080,"","", java.net.Proxy.Type.DIRECT,false);

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource("/nacos.fxml"));
        stage.setScene(new Scene(loader.load(), 1100, 600));
        stage.setTitle("NacosTool   v1.0.0");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static HBox getMenuStage() {
        Label proxyLabel = new Label("代理");
        Label aboutLabel = new Label("关于");
        String msg = "";
        if(proxy.isEnable()){
            msg = proxy.toString();
        }
        Label proxyLabel2 = new Label(msg);
        proxyLabel2.setAlignment(Pos.CENTER);
        proxyLabel.setOnMouseClicked(event -> {
            Stage stage = getProxyStage();
            stage.setOnCloseRequest(e -> {
                CompletableFuture.supplyAsync(() -> {
                    Platform.runLater(()->proxyLabel2.setText(proxy.isEnable() && proxy.getType() !=  java.net.Proxy.Type.DIRECT?proxy.toString():""));
                    return null;
                });
            });
            stage.show();
        });
        aboutLabel.setOnMouseClicked(event -> {
            getAboutStage().show();
        });
        HBox hbox = new HBox(20,proxyLabel,aboutLabel,proxyLabel2);
        hbox.setPadding(new javafx.geometry.Insets(3, 10, 3, 10));
        return hbox;
    }

    private static Stage getProxyStage() {
        VBox basePanel = new VBox();
        basePanel.setAlignment(Pos.CENTER);
        basePanel.setPrefHeight(500.0);
        basePanel.setPrefWidth(500.0);
        basePanel.setSpacing(20.0);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10.0);
        gridPane.setVgap(20.0);

        Label hostLabel = new Label("主机");
        TextField hostField = new TextField("127.0.0.1");
        Label portLabel = new Label("端口");
        TextField portField = new TextField("8080");
        Label usernameLabel = new Label("用户");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("密码");
        TextField passwordField = new TextField();
        Label typeLabel = new Label("类型");
        ComboBox<java.net.Proxy.Type> typeOptions = new ComboBox<>();
        CheckBox enableCheckBox = new CheckBox("启用");

        gridPane.add(hostLabel, 0, 0);
        gridPane.add(hostField, 1, 0, 2, 1);
        gridPane.add(portLabel, 0, 1);
        gridPane.add(portField, 1, 1, 2, 1);
        gridPane.add(usernameLabel, 0, 2);
        gridPane.add(usernameField, 1, 2, 2, 1);
        gridPane.add(passwordLabel, 0, 3);
        gridPane.add(passwordField, 1, 3, 2, 1);
        gridPane.add(typeLabel, 0, 4);
        gridPane.add(typeOptions, 1, 4);
        gridPane.add(enableCheckBox, 2, 4);

        basePanel.getChildren().add(gridPane);
        Utils.setGlobalFocusTraversal(false,basePanel);//取消默认获取焦点

        typeOptions.getItems().setAll(java.net.Proxy.Type.DIRECT, java.net.Proxy.Type.HTTP, java.net.Proxy.Type.SOCKS);
        typeOptions.getSelectionModel().selectFirst();

        hostField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && enableCheckBox.isSelected()) {
                setProxy(hostField, portField, usernameField, passwordField, typeOptions, enableCheckBox);
            }
        });
        portField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && enableCheckBox.isSelected()) {
                setProxy(hostField, portField, usernameField, passwordField, typeOptions, enableCheckBox);
            }
        });
        usernameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && enableCheckBox.isSelected()) {
                setProxy(hostField, portField, usernameField, passwordField, typeOptions, enableCheckBox);
            }
        });
        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && enableCheckBox.isSelected()) {
                setProxy(hostField, portField, usernameField, passwordField, typeOptions, enableCheckBox);
            }
        });
        typeOptions.valueProperty().addListener((observable, oldValue, newValue) -> {
            setProxy(hostField, portField, usernameField, passwordField, typeOptions, enableCheckBox);
        });
        enableCheckBox.setOnAction((event)->{
            setProxy(hostField, portField, usernameField, passwordField, typeOptions, enableCheckBox);
        });

        //初始化值
        hostField.setText(proxy.getHost());
        portField.setText(String.valueOf(proxy.getPort()));
        usernameField.setText(proxy.getUsername());
        passwordField.setText(proxy.getPassword());
        typeOptions.getSelectionModel().select(proxy.getType());
        enableCheckBox.setSelected(proxy.isEnable());

        Stage stage = new Stage();
        Scene scene = new Scene(basePanel, 300, 300);
        stage.setScene(scene);
        stage.setTitle("代理");
        stage.initModality(Modality.APPLICATION_MODAL);//阻塞主窗口
        return stage;
    }

    private static Stage getAboutStage() {
        Hyperlink hyperlink = new Hyperlink("https://github.com/fasnow/nacostool");
        hyperlink.setOnAction(event -> {
            try {
                URI uri = new URI("https://github.com/fasnow/nacostool");
                Desktop.getDesktop().browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        FlowPane pane = new FlowPane(hyperlink);
        pane.setAlignment(Pos.CENTER);
        Utils.setGlobalFocusTraversal(false,pane);//取消默认获取焦点
        Stage stage = new Stage();
        Scene scene = new Scene(pane,300,50);
        stage.setScene(scene);
        stage.setTitle("关于");
        stage.initModality(Modality.APPLICATION_MODAL);//阻塞主窗口
        return stage;
    }

    private static void setProxy(TextField hostField, TextField portField, TextField usernameField, TextField passwordField, ComboBox<java.net.Proxy.Type> typeOptions, CheckBox enableCheckBox){
        CompletableFuture.supplyAsync(() -> {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            String username = usernameField.getText();
            String password = passwordField.getText();
            java.net.Proxy.Type type = typeOptions.getSelectionModel().getSelectedItem();
            boolean enable = enableCheckBox.isSelected();
            Proxy proxy = new Proxy(host,port,username,password,type,enable);
            Application.proxy.setHost(host);
            Application.proxy.setPort(port);
            Application.proxy.setUsername(username);
            Application.proxy.setPassword(password);
            Application.proxy.setType(type);
            Application.proxy.setEnable(enable);
            HttpClient.setProxy(proxy);
            return null;
        });
    }

}