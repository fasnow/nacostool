package org.fasnow.redistool;

import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    public static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded";
    private static final int defaultTimeout = 5;
    private static OkHttpClient httpClient = getClient();

    public static OkHttpClient getClient() {
        if(httpClient==null){
            try {
                // 创建信任所有证书的 TrustManager
                TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // 不检查客户端证书
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // 不检查服务器证书
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};

                // 创建 SSLContext，使用信任所有证书的 TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagers, new java.security.SecureRandom());

                // 设置 OkHttpClient 的 SSLContext
                httpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                        .hostnameVerifier((hostname, session) -> true).connectTimeout(defaultTimeout,TimeUnit.SECONDS).build(); // 忽略主机名验证
            } catch (Exception e) {
                throw new RuntimeException("无法创建OkHttpClient", e);
            }
        }
        return httpClient;
    }

    public static void setTimeout(int timeout){
        httpClient = httpClient.newBuilder().connectTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public static void setProxy(Proxy proxy){
        if(proxy.getType() == java.net.Proxy.Type.DIRECT || !proxy.isEnable()){
            httpClient = httpClient.newBuilder().proxy(java.net.Proxy.NO_PROXY).build();
            return;
        }
        httpClient = httpClient.newBuilder().proxy(new java.net.Proxy(proxy.getType(), new InetSocketAddress(proxy.getHost(), proxy.getPort()))).build();
    }
}
