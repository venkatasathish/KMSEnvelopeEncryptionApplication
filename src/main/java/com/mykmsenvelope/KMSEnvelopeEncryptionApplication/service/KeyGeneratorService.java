package com.mykmsenvelope.KMSEnvelopeEncryptionApplication.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.gcpdemo.mygcpapp.model.ApplicationConstants;
import com.gcpdemo.mygcpapp.model.KMSProperties;
import com.gcpdemo.mygcpapp.model.Key;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

@Service
public class KeyGeneratorService {
	
	@Autowired 
	Firestore db;
	
	@Autowired
	private CryptingService cryptingService;
	
	@Autowired
	private KMSProperties kmsProperties;
	
	@Autowired
	CacheManager cache;
	
	@Cacheable(value="datakeys")
	public Key generateKey() throws NoSuchAlgorithmException {
		
		
		Key ky = new Key();
		byte[] encryptBytes = null;
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(ApplicationConstants.AES_KEY_LENGTH);
			SecretKey key = keyGenerator.generateKey();
		
			encryptBytes = cryptingService.encrypt(kmsProperties.getProjectId(), kmsProperties.getLocationId(), kmsProperties.getKeyRingId(), kmsProperties.getCryptoKeyId(), key.getEncoded());
			System.out.println(keyToString(key)+"   "+new String(Base64.getEncoder().encode(encryptBytes),"UTF-8"));
			ky.setKeyValue(keyToString(key));
			ky.setWrappedKey(new String(Base64.getEncoder().encode(encryptBytes),"UTF-8"));
			DocumentReference addedDocRef = db.collection("dek").document();
			ky.setId(addedDocRef.getId());
			
			System.out.println("Added document with ID: " + addedDocRef.getId());

			addedDocRef.set(ky);
			
			System.out.println(ky.getId()+"   "+ky.getId().getBytes().length);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ky;
		
	}
	
	
	public static String keyToString(SecretKey secretKey) {
		  /* Get key in encoding format */
		  byte encoded[] = secretKey.getEncoded();

		  /*
		   * Encodes the specified byte array into a String using Base64 encoding
		   * scheme
		   */
		  String encodedKey = Base64.getEncoder().encodeToString(encoded);

		  return encodedKey;
	}

	public static SecretKey decodeKeyFromString(String keyStr) {
		  /* Decodes a Base64 encoded String into a byte array */
		  byte[] decodedKey = Base64.getDecoder().decode(keyStr);

		  /* Constructs a secret key from the given byte array */
		  SecretKey secretKey = new SecretKeySpec(decodedKey, 0,
		    decodedKey.length, "AES");

		  return secretKey;
	}

	@Cacheable(value="decryptionkeys")
	public String getKey(String key) throws NoSuchAlgorithmException {
		
		DocumentReference docRef = db.collection("dek").document(key);
		// asynchronously retrieve the document
		ApiFuture<DocumentSnapshot> future = docRef.get();
		// block on response
		DocumentSnapshot document;
		try {
			document = future.get();
			Key ky = null;
			if (document.exists()) {
			  // convert document to POJO
			  ky = document.toObject(Key.class);
			  System.out.println(ky);
			  return ky.getKeyValue();
			} else {
			  System.out.println("No such document!");
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

}
