package com.packt.mqttessentials.Sensors02;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ThreadLocalRandom;

public class SensorsManager implements MqttCallback {
	private static final String SENSOR_EARTH_HUMIDITY = "earthhumidity";
	private static final String SENSOR_SUNLIGHT = "sunlight";
	private static final String TOPIC_SEPARATOR = "/";
	private final String boardCommandsTopic;
	private final String boardDataBaseTopic;
	private final String encoding;
	private final MqttAsyncClient asyncClient;
	private final String earthHumidityTopic;
	private final String visibleLightTopic;
	private final String infraredLightTopic;
	private final String ultraVioletIndexTopic;
	private volatile boolean isSunlightSensorTurnedOn = false;
	private volatile boolean isEarthHumiditySensorTurnedOn = false;
	
	public SensorsManager(final MqttAsyncClient asyncClient, 
			final String boardCommandsTopic, final String boardDataBaseTopic, final String encoding) {
		this.boardCommandsTopic = boardCommandsTopic;
		this.boardDataBaseTopic = boardDataBaseTopic;
		this.encoding = encoding;
		this.asyncClient = asyncClient;
		// Build and save the topic names that we will use to publish the data from the sensors
		this.earthHumidityTopic = this.boardDataBaseTopic.concat(SENSOR_EARTH_HUMIDITY);
		final String sunlightDataBaseTopic = boardDataBaseTopic.concat(SENSOR_SUNLIGHT);
		this.visibleLightTopic = String.join(TOPIC_SEPARATOR, sunlightDataBaseTopic, "visiblelight");
		this.infraredLightTopic = String.join(TOPIC_SEPARATOR, sunlightDataBaseTopic, "ir");
		this.ultraVioletIndexTopic = String.join(TOPIC_SEPARATOR, sunlightDataBaseTopic, "uv");
	}

	public IMqttDeliveryToken publishMessage(final String topic, 
		final String textForMessage, IMqttActionListener actionListener,
		final int qos, final boolean retained) {
		byte[] bytesForPayload;
		try {
			bytesForPayload = textForMessage.getBytes(this.encoding);
			return asyncClient.publish(topic, bytesForPayload, qos, 
				retained, null, actionListener);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (MqttException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void publishProcessedCommandMessage(final String sensorName, final String command) {
		final String topic = String.format("%s/%s", boardCommandsTopic, sensorName);
		final String textForMessage = String.format(
			"%s successfully processed command: %s", sensorName, command);
		publishMessage(topic, textForMessage, null, 0, false);
	}
	
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
		String messageText = new String(message.getPayload(), encoding);
		System.out.println(
			String.format("Topic: %s. Payload: %s",
				topic,
				messageText));
		// A message has arrived from the MQTT broker
		// The MQTT broker doesn't send back 
		// an acknowledgment to the server until 
		// this method returns cleanly
		if (!topic.startsWith(boardCommandsTopic)) {
			// The topic for the arrived message doesn't start with boardTopic
			return;
		}
		final boolean isTurnOnMessage = messageText.equals("TURN ON"); 
		final boolean isTurnOffMessage = messageText.equals("TURN OFF");
		boolean isInvalidCommand = false;
		boolean isInvalidTopic = false;
		// Extract the sensor name from the topic
		String sensorName = topic.replaceFirst(boardCommandsTopic, "").replaceFirst(TOPIC_SEPARATOR, "");
		switch (sensorName) {
			case SENSOR_SUNLIGHT:
				if (isTurnOnMessage) {
					isSunlightSensorTurnedOn = true;
				} else if (isTurnOffMessage) {
					isSunlightSensorTurnedOn = false;
				} else {
					isInvalidCommand = true;
				}
				break;
			case SENSOR_EARTH_HUMIDITY:
				if (isTurnOnMessage) {
					isEarthHumiditySensorTurnedOn = true;
				} else if (isTurnOffMessage) {
					isEarthHumiditySensorTurnedOn = false;
				} else {
					isInvalidCommand = true;
				}
				break;
			default:
				isInvalidTopic = true;
		}
		if (!isInvalidCommand && !isInvalidTopic) {
			publishProcessedCommandMessage(sensorName, messageText);	
		}
	}
	
	public void loop() {
		if (isEarthHumiditySensorTurnedOn) {
			// Retrieve the humidity level from the sensor
			// In this case, we just generate a random number
			final int humidityLevel = ThreadLocalRandom.current().nextInt(1, 101);
			// Publish the message to the appropriate topic
			publishMessage(earthHumidityTopic, 
				String.format("%d %%", humidityLevel), null, 0, false);
		}
		if (isSunlightSensorTurnedOn) {
			// Retrieve the visible light level from the sensor
			// In this case, we just generate a random number
			final int visibleLight = ThreadLocalRandom.current().nextInt(201, 301);
			// Publish the message to the appropriate topic
			publishMessage(visibleLightTopic,
				String.format("%d lm", visibleLight), null, 0, false);

			// Retrieve the infrared light level from the sensor
			// In this case, we just generate a random number
			final int infraredLight = ThreadLocalRandom.current().nextInt(251, 281);
			// Publish the message to the appropriate topic
			publishMessage(infraredLightTopic,
				String.format("%d lm", infraredLight), null, 0, false);

			// Retrieve the ultraviolet (UV) index from the sensor
			// In this case, we just generate a random number
			final int ultraVioletIndex = ThreadLocalRandom.current().nextInt(0, 16);
			// Publish the message to the appropriate topic
			publishMessage(ultraVioletIndexTopic,
				String.format("%d UV Index", ultraVioletIndex), null, 0, false);
		}
	}
}
