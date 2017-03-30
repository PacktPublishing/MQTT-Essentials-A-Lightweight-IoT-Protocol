// package com.packt.mqttessentials.Sensors01;

import java.io.UnsupportedEncodingException;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.OperationsException;

public class ConnectionListener implements IMqttActionListener {
	protected final String name;
	
	public ConnectionListener(String name) {
		this.name = name;
	}
	
	@Override
	public void onSuccess(IMqttToken asyncActionToken) {
		System.out.println(
			String.format(
				"%s successfully connected",
				name));
//		try {
//			subscribeToken = 
//				client.subscribe(
//					TOPIC,
//					QUALITY_OF_SERVICE, 
//					null, 
//					this);
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void onFailure(IMqttToken asyncActionToken, 
		Throwable exception) {
		// The method will run if an operation failed
		exception.printStackTrace();
	}
	
//	public MessageActionListener publishTextMessage(
//		String messageText) {
//		byte[] bytesMessage;
//		try {
//			bytesMessage = 
//				messageText.getBytes(ENCODING);
//			MqttMessage message;
//			message = new MqttMessage(bytesMessage);
//			String userContext = "ListeningMessage";
//			MessageActionListener actionListener = 
//				new MessageActionListener(
//					TOPIC,
//					messageText,
//					userContext);
//			client.publish(TOPIC,
//				message,
//				userContext, 
//				actionListener);
//			return actionListener;
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			return null;
//		} catch (MqttException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
}
