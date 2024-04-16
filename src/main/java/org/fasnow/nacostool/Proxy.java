package org.fasnow.nacostool;

public class Proxy {

    private String host;
    private int port;
    private String username;
    private String password;
    private java.net.Proxy.Type type;

    private boolean enable;

    public Proxy(String host, int port, String username, String password, java.net.Proxy.Type type,boolean enable) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.type = type;
        this.enable = enable;
    }

    public Proxy(){

    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(java.net.Proxy.Type type) {
        this.type = type;
    }

    public java.net.Proxy.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String s;
        if(username !=null && !username.equals("") && password != null && !password.equals("")){
            s = String.format("%s://%s:%s@%s:%s",type.toString().toLowerCase(),username,password,host,port);
        }else {
            s = String.format("%s://%s:%s",type.toString().toLowerCase(),host,port);
        }
        if(enable){
            s="代理已启用:"+s;
        }else {
            s="代理未启用:"+s;
        }
        return s;
    }
}
