// package com.packt.mqttessentials.Sensors01;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SubscriberCallback implements MqttCallback {
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
		if (!topic.equals("commands/leds/led01")) {
			return;
		}
		String messageText = 
			new String(message.getPayload(), "UTF-8");
		System.out.println(
			String.format("%s received %s: %s",
				"led01",
				topic,
				messageText));
//		String[] keyValue = 
//			messageText.split(COMMAND_SEPARATOR);
//		if (keyValue.length != 3) {
//			return;
//		}
//		if (keyValue[0].equals(COMMAND_KEY) &&
//			keyValue[1].equals(
//				GET_ALTITUDE_COMMAND_KEY) &&
//			keyValue[2].equals(name)) {
//			// Process the get altitude command
//			int altitudeInFeet = ThreadLocalRandom
//				.current().nextInt(1, 6001);
//			System.out.println(
//			  String.format("%s altitude: %d feet",
//				name,
//				altitudeInFeet));
//		}
	}
}
