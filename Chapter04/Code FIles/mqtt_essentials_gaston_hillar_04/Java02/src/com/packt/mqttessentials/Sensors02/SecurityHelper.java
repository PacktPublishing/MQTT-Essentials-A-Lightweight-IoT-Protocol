package com.packt.mqttessentials.Sensors02;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;


public class SecurityHelper {
	private final static String TLS_VERSION = "TLSv1.2";
	
	private static KeyFactory getKeyFactoryInstance() throws NoSuchAlgorithmException {
		return KeyFactory.getInstance("RSA");
	}
	
    private static X509Certificate createX509CertificateFromFile(
    	final String certificateFileName) throws IOException, CertificateException 
    {
        // Load an X509 certificate from the specified certificate file name
		final File file = new java.io.File(certificateFileName);
		if (!file.isFile()) {
			throw new IOException(
				String.format("The certificate file %s doesn't exist.", certificateFileName));
		}
     	final CertificateFactory certificateFactoryX509 = CertificateFactory.getInstance("X.509");
     	final InputStream inputStream = new FileInputStream(file);
        final X509Certificate certificate = (X509Certificate) certificateFactoryX509.generateCertificate(inputStream);
        inputStream.close();
        
        return certificate;
    }

    private static PrivateKey createPrivateKeyFromPemFile(final String keyFileName) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException 
    {
    	// Loads a privte key from the specified key file name
        final PemReader pemReader = new PemReader(new FileReader(keyFileName));
        final PemObject pemObject = pemReader.readPemObject();
        final byte[] pemContent = pemObject.getContent();
        pemReader.close();
        final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemContent);
        final KeyFactory keyFactory = getKeyFactoryInstance();
        final PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
        return privateKey;
    }

	private static KeyManagerFactory createKeyManagerFactory(
		final String clientCertificateFileName, final String clientKeyFileName, final String clientKeyPassword) 
		throws InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException
	{
		// Creates a key manager factory
		// Load and create the client certificate
		final X509Certificate clientCertificate = createX509CertificateFromFile(clientCertificateFileName);	
		// Load the private client key
		final PrivateKey privateKey = createPrivateKeyFromPemFile(clientKeyFileName);
		// Client key and certificate are sent to server
		final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setCertificateEntry("certificate", clientCertificate);
		keyStore.setKeyEntry("private-key", privateKey, 
			clientKeyPassword.toCharArray(),
			new Certificate[] { clientCertificate });
		final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, clientKeyPassword.toCharArray());
		
		return keyManagerFactory;
	}

	private static TrustManagerFactory createTrustManagerFactory(
		final String caCertificateFileName) 
		throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException 
	{
		// Creates a trust manager factory
		// Load CA certificate
		final X509Certificate caCertificate = (X509Certificate) createX509CertificateFromFile(caCertificateFileName);
		// CA certificate is used to authenticate server
		final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca-certificate", caCertificate);
		final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		return trustManagerFactory;
	}
	
	// caCertificate
	// clientCertificate 
	// clientKey
	public static SSLSocketFactory createSocketFactory(
		final String caCertificateFileName, 
		final String clientCertificateFileName, 
		final String clientKeyFileName) throws Exception
	{
		// Creates a TLS socket factory with the given 
		// CA certificate file, client certificate, client key
		// In this case, we are working without a client key password
		final String clientKeyPassword = "";
		try
		{
			Security.addProvider(new BouncyCastleProvider());
			final KeyManager[] keyManagers = createKeyManagerFactory(clientCertificateFileName, clientKeyFileName, clientKeyPassword).getKeyManagers();
			final TrustManager[] trustManagers = createTrustManagerFactory(caCertificateFileName).getTrustManagers();
			
			// Create the TLS socket factory for the desired TLS version
			final SSLContext context = SSLContext.getInstance(TLS_VERSION);
			
			context.init(keyManagers, trustManagers, new SecureRandom());
			//context.init(keyManagers, trustManagers, null);
	
			return context.getSocketFactory();			
		}
		catch (Exception e)
		{
			throw new Exception("I cannot create the TLS socket factory.", e);
		}		
	}
}
