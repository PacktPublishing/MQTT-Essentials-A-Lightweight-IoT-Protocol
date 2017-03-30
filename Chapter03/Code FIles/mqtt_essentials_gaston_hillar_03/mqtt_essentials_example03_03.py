from command import *
import paho.mqtt.client as mqtt
import os.path
import time
import json


# Replace /Users/gaston/certificates with the path
# in which you saved the certificate authoritity file,
# the client certificate file and the client key
certificates_path = "/Users/gaston/certificates"
ca_certificate = os.path.join(certificates_path, "ca.crt")
client_certificate = os.path.join(certificates_path, "device001.crt")
client_key = os.path.join(certificates_path, "device001.key")
mqtt_server_host = "localhost"
mqtt_server_port = 8883
mqtt_keepalive = 60

drone_name = "drone01"
commands_topic = "commands/{}".format(drone_name)
processed_commands_topic = "processedcommands/{}".format(drone_name)


class LoopControl:
    is_last_command_processed = False


def on_connect(client, userdata, rc):
    print("Connect result: {}".format(mqtt.connack_string(rc)))
    client.subscribe(processed_commands_topic)


def on_message(client, userdata, msg):
    if msg.topic == processed_commands_topic:
        payload_string = msg.payload.decode('utf-8')
        print(payload_string)
        if payload_string.count(CMD_LAND_IN_SAFE_PLACE) > 0:
            LoopControl.is_last_command_processed = True


def on_subscribe(client, userdata, mid, granted_qos):
    print("Subscribed with QoS: {}".format(granted_qos[0]))


def publish_command(client, command_name, key="", value=""):
    if key:
        command_message = json.dumps({
            COMMAND_KEY: command_name,
            key: value})
    else:
        command_message = json.dumps({
            COMMAND_KEY: command_name})
    result = client.publish(topic=commands_topic,
                            payload=command_message, qos=2)
    return result


if __name__ == "__main__":
    client = mqtt.Client(protocol=mqtt.MQTTv311)
    client.on_connect = on_connect
    client.on_subscribe = on_subscribe
    client.on_message = on_message
    client.tls_set(ca_certs = ca_certificate,
        certfile=client_certificate,
        keyfile=client_key)
    client.connect(host=mqtt_server_host,
        port=mqtt_server_port,
        keepalive=mqtt_keepalive)
    publish_command(client, CMD_TAKE_OFF)
    publish_command(client, CMD_MOVE_UP)
    publish_command(client, CMD_ROTATE_LEFT, KEY_DEGREES, 90)
    publish_command(client, CMD_ROTATE_LEFT, KEY_DEGREES, 45)
    publish_command(client, CMD_ROTATE_LEFT, KEY_DEGREES, 45)
    publish_command(client, CMD_LAND_IN_SAFE_PLACE)
    while LoopControl.is_last_command_processed == False:
        client.loop()
        time.sleep(1)
    client.disconnect()
    client.loop()
