package com.nutanix.pri.java.client.auth;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

@Slf4j
@Getter
public class MutualTLSConfig {

    private final KeyStore keyStore;
    private final KeyStore trustKeyStore;
    private final String keyStorePassword;
    private final String trustStorePassword;
    private final String privateKeyPassword;
    private String privateKeyAlias;

    private static final String KEY_STORE_TYPE = "pkcs12";

    public MutualTLSConfig(String trustStorePath, String trustStorePassword, String keyStorePath,
        String keyStorePassword, String privateKeyPassword, String privateKeyAlias) throws Exception {
        this.privateKeyAlias = privateKeyAlias;
        log.debug("Loading JKS keystore...");
        try (
            FileInputStream keyStoreFile = new FileInputStream(keyStorePath);
            FileInputStream trustKeyStoreFile = new FileInputStream(trustStorePath)
        ) {
            if (StringUtils.isBlank(keyStorePassword) ||
                StringUtils.isBlank(trustStorePassword) ||
                StringUtils.isBlank(privateKeyPassword)) {
                throw new KeyStoreException("Passwords are not provided to access java keystores!");
            }

            this.keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            this.trustKeyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            this.keyStorePassword = keyStorePassword;
            this.trustStorePassword = trustStorePassword;
            this.privateKeyPassword = privateKeyPassword;
            this.keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
            this.trustKeyStore.load(trustKeyStoreFile, this.trustStorePassword.toCharArray());
        } catch (Exception e) {
            log.error("Failed to load java trust and key stores due to ", e);
            throw e;
        }
    }

    public SSLConnectionSocketFactory getSSLConnectionSocket() throws Exception {
        SSLContext sslContext = SSLContexts.custom()
            // load identity keystore
            .loadKeyMaterial(this.keyStore, this.privateKeyPassword.toCharArray(), (aliases, socket) -> this.privateKeyAlias)
            // load trust keystore
            .loadTrustMaterial(this.trustKeyStore, null)
            .build();

        return new SSLConnectionSocketFactory(sslContext,
            new String[]{"TLSv1.2", "TLSv1.1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

}