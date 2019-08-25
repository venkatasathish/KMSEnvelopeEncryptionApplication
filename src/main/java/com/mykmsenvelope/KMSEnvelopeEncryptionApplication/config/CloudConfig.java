package com.mykmsenvelope.KMSEnvelopeEncryptionApplication.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.kms.v1.KeyManagementServiceSettings;
import com.google.common.collect.Lists;

@Configuration
public class CloudConfig {
	
	@Autowired 
	KeyManagementServiceSettings keyManagementServiceSettings;
	
	@Autowired 
	Firestore firestore;
	
	@Bean
	public Firestore getFirestore() {
		
		try {
			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\M1047094\\Desktop\\Sathish\\gcp-demo-247008-434118fbc4d5.json"))
			        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
			firestore =  FirestoreOptions.getDefaultInstance().newBuilder().setTimestampsInSnapshotsEnabled(true).setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build().getService();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return firestore;
		
	}
	
	@Bean
	public KeyManagementServiceSettings getKeyManagementServiceSettings() {
		
		try {
			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\M1047094\\Desktop\\Sathish\\gcp-demo-247008-434118fbc4d5.json"))
			        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
			
			keyManagementServiceSettings =  KeyManagementServiceSettings.newBuilder()
					       .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
					         .build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyManagementServiceSettings;
	}

}