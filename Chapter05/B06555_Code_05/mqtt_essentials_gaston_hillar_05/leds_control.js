/*
Book: MQTT Essentials
Chapter 5: Control home automation in JavaScript
Author: Gaston C. Hillar - Twitter.com/gastonhillar
Publisher: Packt Publishing Ltd. - http://www.packtpub.com
*/

var mqtt = require("mqtt")


// Replace with the host name for the MQTT Server
var host = "localhost"
// Replace with the port number for MQTT over WebSockets the MQTT Server
var port = 9001
// If we want to work with TLS, we must use the next line
// because we must use wss:// instead of ws://
//var client = mqtt.connect("wss://" + host + ":" + port)
var client = mqtt.connect("ws://" + host + ":" + port)

var ledCommandBaseTopic = "home/leds/"
var ledResultBaseTopic = "home/results/leds/"

client.on("connect", function () {
    console.log("I'm connected to the MQTT server")
    var topicFilters = Array()
    for (i = 1; i < 4; i++) {
        topicFilters.push(ledCommandBaseTopic + i)
    }
    client.subscribe(topicFilters)
})
 
client.on('message', function (topic, message) {
    // message is Buffer 
    var payloadString = message.toString()
    console.log("Message arrived for topic: " + topic + ", with the following payload: " + payloadString)
    if (!topic.startsWith(ledCommandBaseTopic)) {
        return;
    }
    var ledNumber = topic.replace(ledCommandBaseTopic, "")
    var payload = JSON.parse(payloadString)
    if (ledNumber && payload.Color) {
        console.log("I've changed LED #" + ledNumber + " to " + payload.Color)
    }
    var resultMessagePayload = {
        "Color": payload.Color
    }
    resultMessagePayloadString = JSON.stringify(resultMessagePayload)

    client.publish(ledResultBaseTopic + ledNumber, resultMessagePayloadString)
})
