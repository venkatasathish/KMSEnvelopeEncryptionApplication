package com.mykmsenvelope.KMSEnvelopeEncryptionApplication.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyManagementServiceSettings;
import com.google.protobuf.ByteString;

@Service
public class CryptingService {
	
	@Autowired 
	KeyManagementServiceSettings keyManagementServiceSettings;
	
	@Autowired
	private KeyGeneratorService keyGeneratorService;
	
	private static final String salt = "herdyrjhjaiwkaop";
	
	/**
	 * Encrypts the given plaintext using the specified crypto key.
	 */
	public byte[] encrypt(
	    String projectId, String locationId, String keyRingId, String cryptoKeyId, byte[] plaintext)
	    throws IOException {

	  // Create the KeyManagementServiceClient using try-with-resources to manage client cleanup.
	  try (KeyManagementServiceClient client = KeyManagementServiceClient.create(keyManagementServiceSettings)) {

	    // The resource name of the cryptoKey
	    String resourceName = CryptoKeyName.format(projectId, locationId, keyRingId, cryptoKeyId);

	    // Encrypt the plaintext with Cloud KMS.
	    EncryptResponse response = client.encrypt(resourceName, ByteString.copyFrom(plaintext));

	    // Extract the ciphertext from the response.
	    return response.getCiphertext().toByteArray();
	  }
	}
	//projects/gcp-demo-247008/locations/us-central1/keyRings/cloudkmstest/cryptoKeys/cloudkmstestkey
	
	/**
	 * Decrypts the provided ciphertext with the specified crypto key.
	 */
	public byte[] decrypt(
	    String projectId, String locationId, String keyRingId, String cryptoKeyId, byte[] ciphertext)
	    throws IOException {

	  // Create the KeyManagementServiceClient using try-with-resources to manage client cleanup.
	  try (KeyManagementServiceClient client = KeyManagementServiceClient.create(keyManagementServiceSettings)) {

	    // The resource name of the cryptoKey
	    String resourceName = CryptoKeyName.format(projectId, locationId, keyRingId, cryptoKeyId);

	    // Decrypt the ciphertext with Cloud KMS.
	    DecryptResponse response = client.decrypt(resourceName, ByteString.copyFrom(ciphertext));

	    // Extract the plaintext from the response.
	    return response.getPlaintext().toByteArray();
	  }
	}
	
	
	
	 /**
     * This method will encrypt the given data
     * @param key : the password that will be used to encrypt the data
     * @param data : the data that will be encrypted
     * @return Encrypted data in a byte array
	 * @throws UnsupportedEncodingException 
     */
    public  byte [] encryptData(String key,String keyId, byte [] data) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {

        //Prepare the nonce
        SecureRandom secureRandom = new SecureRandom();

        //Noonce should be 12 bytes
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        //Encryption mode on!
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        //Encrypt the data
        byte [] encryptedData = cipher.doFinal(data);

        //Concatenate everything and return the final data
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + 20 +encryptedData.length);
        byteBuffer.putInt(iv.length);
        byteBuffer.put(iv);
        byteBuffer.put(keyId.getBytes("UTF-8"));
        byteBuffer.put(encryptedData);
        return byteBuffer.array();
    }


    public  byte [] decryptData( byte [] encryptedData) 
            throws NoSuchPaddingException, 
            NoSuchAlgorithmException, 
            InvalidAlgorithmParameterException, 
            InvalidKeyException, 
            BadPaddingException, 
            IllegalBlockSizeException, 
            InvalidKeySpecException {

    	
        //Wrap the data into a byte buffer to ease the reading process
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);

        int noonceSize = byteBuffer.getInt();

        //Make sure that the file was encrypted properly
        if(noonceSize < 12 || noonceSize >= 16) {
            throw new IllegalArgumentException("Nonce size is incorrect. Make sure that the incoming data is an AES encrypted file.");
        }
        byte[] iv = new byte[noonceSize];
        byte[] keyId = new byte[20];
        byteBuffer.get(iv);
        byteBuffer.get(keyId);
        
        String key = keyGeneratorService.getKey(new String(keyId));
        

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
      
        //get the rest of encrypted data
        byte[] cipherBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherBytes);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        //Encryption mode on!
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        //Encrypt the data
        return cipher.doFinal(cipherBytes);

    }
    
    
    /**
     * Reads the file in the given path into a byte array
     * @param path: Path to the file, including the file name. For example: "C:/myfolder/myfile.txt"
     * @return byte array of the file data
     * @throws IOException
     */
    public static byte[] readFile(String path) throws IOException {
 
        File file = new File(path);
 
        byte [] fileData = new byte[(int) file.length()];
 
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileData);
        }
 
        return fileData;
    }
 
    /**
     * Writes a file with the given data into a file with the given path
     * @param path: Path to the file to be created, including the file name. For example: "C:/myfolder/myfile.txt"
     * @param data: byte array of the data to be written
     * @throws IOException
     */
    public static void writeFile(String path, byte [] data) throws IOException {
 
        try(FileOutputStream fileOutputStream = new FileOutputStream(path)) {
            fileOutputStream.write(data);
        }
 
    }
	

}
