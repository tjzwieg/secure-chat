package com.elementzero.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Enumeration;

import org.apache.commons.codec.binary.Base64;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class KeyCertService {

	private enum KeyStoreType {
		TRUSTED,
		LOCAL,
		NewLOCAL
	}
	
	private static KeyCertService instance;
	private static String keystoreFileName = "secureChatKeyStore.bin";
	private static String trustedKeyStoreFileName = "elementZeroKeyStore.jks";
	private static String certAuthAlias = "elementzero";
	private static char[] keystorePassword = new char[]{ '5','p','7','c','D','g','y','4','P','Z','C','V' };
	private static char[] trustedKeyStorePassword = new char[]{ '8','c','9','d','G','f','m','2','L','q','x','I' };
	private static char[] certAuthPassword = new char[]{ 'B','W','Q','Z','F','c','4','J','d','q','7','t' };
	
	private KeyCertService()
	{
		ensureLocalKeyStoreExists();
		
		// Ensure trusted key store and trusted key exists
		try {
			FileInputStream ip = new FileInputStream(trustedKeyStoreFileName);
			ip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static KeyCertService getInstance()
	{
		if (instance == null)
			instance = new KeyCertService();
		return instance;
	}
	
	public KeyPair getKeyPair(String certAlias, String passwordHash) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
	{
		KeyStore keystore = getKeyStore(KeyStoreType.LOCAL);

		Key key = keystore.getKey(certAlias, passwordHash.toCharArray());
		if (key instanceof PrivateKey) {
			// Get certificate of public key
			Certificate cert = keystore.getCertificate(certAlias);

			// Get public key
			PublicKey publicKey = cert.getPublicKey();

			// Return a key pair
			return new KeyPair(publicKey, (PrivateKey) key);
		}
		return null;
	}
	
	public boolean doesKeyExist(String certAlias, String passwordHash) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
	{
		KeyStore keystore = getKeyStore(KeyStoreType.LOCAL);
		if (keystore.containsAlias(certAlias))
		{
			Key key = keystore.getKey(certAlias, passwordHash.toCharArray());
			return (key instanceof PrivateKey);
		}
		return false;
	}
	
	public PublicKey generatePublicKey(String base64PublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		byte[] publicKeyBytes = Base64.decodeBase64(base64PublicKey);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;
	}
	
	public boolean generateCertificate(String certAlias, String certPassword) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, 
			UnrecoverableKeyException, InvalidKeyException, NoSuchProviderException, SignatureException
	{
	    KeyStore keyStore = getKeyStore(KeyStoreType.LOCAL);
	    KeyStore trustedKeyStore = getKeyStore(KeyStoreType.TRUSTED);

	    PrivateKey certAuthPK = (PrivateKey) trustedKeyStore.getKey(certAuthAlias, certAuthPassword);
	    Certificate certAuthCert = trustedKeyStore.getCertificate(certAuthAlias);
	    if (certAuthCert == null)
	    	return false;
	    
	    X509CertImpl certAuthCertImpl = new X509CertImpl(certAuthCert.getEncoded());
	    X509CertInfo certAuthCertInfo = (X509CertInfo) certAuthCertImpl.get(X509CertImpl.NAME + "." + X509CertImpl.INFO);
	    X500Name issuer = (X500Name) certAuthCertInfo.get(X509CertInfo.SUBJECT + "." + CertificateIssuerName.DN_NAME);
	    
	    CertAndKeyGen keyPair = new CertAndKeyGen("RSA", "SHA256WithRSA");
        X500Name x500Name = new X500Name(issuer.getCommonName(), issuer.getOrganizationalUnit(), issuer.getOrganization(), 
        		issuer.getLocality(), issuer.getState(), issuer.getCountry());

        keyPair.setRandom(new SecureRandom());
        keyPair.generate(2048);
        PrivateKey privKey = keyPair.getPrivateKey();
        
        X509Certificate localCert = keyPair.getSelfCertificate(x500Name, new Date(), (long) 365 * 24 * 60 * 60 * 1000);
        
        X509CertImpl localCertImpl = new X509CertImpl(localCert.getEncoded());
	    X509CertInfo localCertInfo = (X509CertInfo) localCertImpl.get(X509CertImpl.NAME + "." + X509CertImpl.INFO);	    
	    X509CertImpl signedCert = new X509CertImpl(localCertInfo);
	    signedCert.sign(certAuthPK, "SHA256WithRSA");
        
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = signedCert;

        keyStore.setKeyEntry(certAlias, privKey, certPassword.toCharArray(), chain);
        setKeyStore(keyStore, KeyStoreType.LOCAL);
	    
//	    PrivateKey privateKey = (PrivateKey) keyStore.getKey(certAlias, certPassword.toCharArray());
//	    Certificate localCert = keyStore.getCertificate(certAlias);
//	    X509CertImpl localCertImpl = new X509CertImpl(localCert.getEncoded());
//	    X509CertInfo localCertInfo = (X509CertInfo) localCertImpl.get(X509CertImpl.NAME + "." + X509CertImpl.INFO);
//	    
//	    Date firstDate = new Date();
//	    Date lastDate = new Date(firstDate.getTime() + 365 * 24 * 60 * 60 * 1000L);
//	    CertificateValidity interval = new CertificateValidity(firstDate, lastDate);
//	    AlgorithmId algorithm = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
//
//	    localCertInfo.set(X509CertInfo.VALIDITY, interval);
//	    localCertInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber((int) (firstDate.getTime() / 1000)));
//	    localCertInfo.set(X509CertInfo.ISSUER + "." + CertificateSubjectName.DN_NAME, issuer);
//	    localCertInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithm);
//	    
//	    X509CertImpl signedCert = new X509CertImpl(localCertInfo);
//	    signedCert.sign(certAuthPK, "SHA256withRSA");
//	    
//	    keyStore.setKeyEntry(certAlias, privateKey, certPassword.toCharArray(), new java.security.cert.Certificate[] { signedCert });
//
//	    setKeyStore(keyStore, KeyStoreType.LOCAL);
	    
	    return true;
	}
	
	public void getValidCertAliases() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		KeyStore keyStore = getKeyStore(KeyStoreType.LOCAL);
		Enumeration<String> keyAliases = keyStore.aliases();
		while (keyAliases.hasMoreElements())
		{
			String alias = keyAliases.nextElement();
			System.out.println("Local Alias: " + alias);
		}
	}
	
	public String generateVerificationKeyCertAlias(String username) throws UnknownHostException
	{
		return generateVerificationKeyCertAlias(getLocalDeviceName(), username);
	}
	
	public String generateVerificationKeyCertAlias(String deviceId, String username)
	{
		return String.format("%s_%s_valid", deviceId, username);
	}
	
	public String generateMessageKeyCertAlias(String username) throws UnknownHostException
	{
		return generateMessageKeyCertAlias(getLocalDeviceName(), username);
	}
	
	public String generateMessageKeyCertAlias(String deviceId, String username)
	{
		return String.format("%s_%s_msg", deviceId, username);
	}
	
	public String getLocalDeviceName() throws UnknownHostException
	{
		return InetAddress.getLocalHost().getHostName();
	}
	
	private void ensureLocalKeyStoreExists() 
	{
		// Ensure local key store exists
		try {
			FileInputStream ip = new FileInputStream(keystoreFileName);
			ip.close();
			
//					KeyStore keyStore = getKeyStore(KeyStoreType.LOCAL);
//					
//					Enumeration<String> keyAliases = keyStore.aliases();
//					while (keyAliases.hasMoreElements())
//					{
//						String alias = keyAliases.nextElement();
//						System.out.println("Local Alias: " + alias);
//					}
		} catch (FileNotFoundException e) {
			try {
				setKeyStore(getKeyStore(KeyStoreType.NewLOCAL), KeyStoreType.LOCAL);
			} catch (Exception innerException) {
				innerException.printStackTrace();
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}
	
	private KeyStore getKeyStore(KeyStoreType type) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		FileInputStream input = null;
		char[] password = null;
		switch (type)
		{
		case LOCAL:
			input = new FileInputStream(keystoreFileName);
			password = keystorePassword;
			break;
		case TRUSTED:
			input = new FileInputStream(trustedKeyStoreFileName);
			password = trustedKeyStorePassword;
			break;
		case NewLOCAL:
			password = keystorePassword;
			break;
		}
	    KeyStore keyStore = KeyStore.getInstance("JKS");
	    keyStore.load(input, password);
	    
	    if (input != null)
	    	input.close();
	    
	    return keyStore;
	}
	
	@SuppressWarnings("incomplete-switch")
	private void setKeyStore(KeyStore keyStore, KeyStoreType type) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		String filename = null;
		char[] password = null;
		
		switch (type)
		{
		case LOCAL:
			filename = keystoreFileName;
			password = keystorePassword;
			break;
		case TRUSTED:
			filename = trustedKeyStoreFileName;
			password = trustedKeyStorePassword;
			break;
		}
		
		FileOutputStream output = new FileOutputStream(filename);
	    keyStore.store(output, password);
	    output.close();
	}
}
