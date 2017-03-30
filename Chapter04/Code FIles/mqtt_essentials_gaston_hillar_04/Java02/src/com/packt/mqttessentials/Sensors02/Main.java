package com.packt.mqttessentials.Sensors02;

import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class Main {
	private final static String BOARD_NAME = "location001";
	private final static String ENCODING_FOR_PAYLOAD = "UTF-8";
	
	public static void main(String[] args) {
		final String boardCommandsTopic = String.format("commands/boards/%s", BOARD_NAME);
		final String boardDataBaseTopic = String.format("data/boards/%s/", BOARD_NAME);
		final String boardStatusTopic = String.format("status/boards/%s", BOARD_NAME);
		final String commandsTopicFilter = String.format("%s/+", boardCommandsTopic);
		
		try {
			final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
			mqttConnectOptions.setMqttVersion(
				MqttConnectOptions.MQTT_VERSION_3_1_1);
			mqttConnectOptions.setKeepAliveInterval(
				MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);

			// Last will message configuration
			final String lastWillMessageText = "OFFLINE";
			byte[] bytesForPayload;
			try {
				bytesForPayload = lastWillMessageText.getBytes(ENCODING_FOR_PAYLOAD);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}
			mqttConnectOptions.setWill(boardStatusTopic, bytesForPayload, 2, true);
			
			MqttDefaultFilePersistence filePersistence = new MqttDefaultFilePersistence();

			// Replace localhost with the IP for the Mosquitto server
			// or other MQTT server
			final String mqttServerHost = "localhost";

			final int mqttServerPort = 8883;
			final String mqttServerURI = String.format(
				"ssl://%s:%d",
				mqttServerHost,
				mqttServerPort);

			// In case you don't need security:
			// final int mqttServerPort = 8883;
			// final String mqttServerURI = String.format(
			//     "ftp://%s:%d",
			//     mqttServerHost,
			//     mqttServerPort);

			// We can specify our own client id 
			// instead of calling the generateClientId method
			//final String mqttClientId = MqttAsyncClient.generateClientId();
			final String mqttClientId = BOARD_NAME;
			MqttAsyncClient mqttAsyncClient = new MqttAsyncClient(
					mqttServerURI, 
					mqttClientId,
					filePersistence);
			
			
			
			// Replace /Users/gaston/certificates with the path
			// in which you saved the certificate authority file,
			// the client certificate file and the client key
			final String certificatesPath = "/users/gaston/Downloads/mosquitto_certs/certificates2";
			final String caCertificateFileName = String.join(java.io.File.separator, certificatesPath, "ca.crt");
			final String clientCertificateFileName = String.join(java.io.File.separator, certificatesPath, "device001.crt");
			final String clientKeyFileName = String.join(java.io.File.separator, certificatesPath, "device001.key");
			// In case you don't need security,
			// you don't need to create an SSLSocketFactory instance
			SSLSocketFactory socketFactory;
			try {
				socketFactory = SecurityHelper.createSocketFactory(
						caCertificateFileName, 
						clientCertificateFileName, 
						clientKeyFileName);
			} catch (Exception e1) {
				e1.printStackTrace();
				return;
			}
			// In case you don't need security, you don't have to call
			// the setSocketFactory method
			mqttConnectOptions.setSocketFactory(socketFactory);
			
			SensorsManager sensorsManager = new SensorsManager(mqttAsyncClient, 
				boardCommandsTopic, boardDataBaseTopic, ENCODING_FOR_PAYLOAD);
			
			mqttAsyncClient.setCallback(sensorsManager);
			
			// In this case, we don't use the token
			IMqttToken mqttConnectToken = mqttAsyncClient.connect(
				mqttConnectOptions, 
				null, 
				new IMqttActionListener() {
					@Override
					public void onSuccess(IMqttToken asyncActionToken) {
						System.out.println(
							String.format(
								"Successfully connected"));
							try {
								// In this case, we don't use the token
								IMqttToken subscribeToken = mqttAsyncClient.subscribe(
									commandsTopicFilter,
									2, 
									null, 
									new IMqttActionListener() {
										@Override
										public void onSuccess(IMqttToken asyncActionToken) {
											System.out.println(
													String.format(
														"Subscribed to the %s topic with QoS: %d",
														asyncActionToken.getTopics()[0],
														asyncActionToken.getGrantedQos()[0]));
											// Publish a retained message indicating the board is online
											sensorsManager.publishMessage(boardStatusTopic, "ONLINE", null, 2, true);
										}
										
										@Override
										public void onFailure(IMqttToken asyncActionToken, 
												Throwable exception) {
											exception.printStackTrace();
										}
									});
							} catch (MqttException e) {
								e.printStackTrace();
							}
					}

					@Override
					public void onFailure(IMqttToken asyncActionToken, 
						Throwable exception) {
						// This method is fired when an operation failed
						exception.printStackTrace();
					}
				});
			
	        while (true) {
	        	sensorsManager.loop();
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                System.err.format(
	                    "Sleep interruption: %s",
	                    e.toString());
	            }
	        }
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
