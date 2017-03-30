//package com.packt.mqttessentials.Sensors01;

import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.util.Properties;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class Main {
	public static void main(String[] args) {
		try {
			final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
			mqttConnectOptions.setMqttVersion(
				MqttConnectOptions.MQTT_VERSION_3_1_1);
			mqttConnectOptions.setKeepAliveInterval(
				MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);

			// Last will message configuration
//			final String messageText = "TURN OFF PLEASE";
//			byte[] bytesMessage;
//			try {
//				bytesMessage = 
//					messageText.getBytes("UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//				return;
//			}
//			mqttConnectOptions.setWill("controllers/lights", bytesMessage, 0, false);
			
			MemoryPersistence memoryPersistence = new MemoryPersistence();

			// Replace localhost with the IP for the Mosquitto
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
			final String mqttClientId = MqttAsyncClient.generateClientId();
			MqttAsyncClient mqttAsyncClient = new MqttAsyncClient(
					mqttServerURI, 
					mqttClientId, 
					memoryPersistence);
			final String topic_for_led01 = "commands/lights/led01";
			
			
			
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
			
			/// ************ UP TO HERE
			mqttAsyncClient.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable cause) {
					// The MQTT client lost the connection
					cause.printStackTrace();
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// Delivery for a message has been completed
					// and all acknowledgments have been received
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					// A message has arrived from the MQTT broker
					// The MQTT broker doesn't send back 
					// an acknowledgment to the server until 
					// this method returns cleanly
					if (!topic.equals(topic_for_led01)) {
						return;
					}
					String messageText = 
						new String(message.getPayload(), "UTF-8");
					System.out.println(
						String.format("Topic: %s. Payload: %s",
							topic,
							messageText));
				}
			});
			
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
									topic_for_led01,
									0, 
									null, 
									new IMqttActionListener() {
										@Override
										public void onSuccess(IMqttToken asyncActionToken) {
											System.out.println(
													String.format(
														"Subscribed to the %s topic with QoS: %d",
														topic_for_led01,
														asyncActionToken.getGrantedQos()[0]));
											
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
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
