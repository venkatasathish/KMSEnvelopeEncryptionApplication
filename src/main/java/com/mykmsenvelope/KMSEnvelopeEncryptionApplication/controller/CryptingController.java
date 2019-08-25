package com.mykmsenvelope.KMSEnvelopeEncryptionApplication.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gcpdemo.mygcpapp.model.KMSProperties;
import com.gcpdemo.mygcpapp.model.Key;
import com.gcpdemo.mygcpapp.service.CryptingService;
import com.gcpdemo.mygcpapp.service.KeyGeneratorService;

@RestController
@RequestMapping(path = "/crypting")
public class CryptingController {
	
	@Autowired
	private KeyGeneratorService keyGeneratorService;
	
	@Autowired
	private CryptingService cryptingService;
	
	@Autowired
	private KMSProperties kmsProperties;
	
	@PostMapping(path= "symmetric/encrypt", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> symmetricEncryptObject(@RequestBody String jsonObject)
    {
		
		byte[] encryptBytes = null;
			
			try {
				Key key = keyGeneratorService.generateKey();
				
				encryptBytes = cryptingService.encryptData(key.getKeyValue(),key.getId(), jsonObject.getBytes("UTF-8"));
				return new ResponseEntity<>(new String(Base64.getEncoder().encode(encryptBytes),"UTF-8"),HttpStatus.ACCEPTED);
				
			} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
					| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
					| InvalidKeySpecException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		
		return null;
		
    }
	
	
	@PostMapping(path= "symmetric/decrypt", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> symmetricDecryptObject(@RequestBody String jsonObject)
    {
		
		byte[] decryptBytes = null;
			try {
				decryptBytes = cryptingService.decryptData(Base64.getDecoder().decode(jsonObject));
				
			} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
					| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
					| InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return new ResponseEntity<>(new String(decryptBytes),HttpStatus.ACCEPTED);
	
    }
	
}