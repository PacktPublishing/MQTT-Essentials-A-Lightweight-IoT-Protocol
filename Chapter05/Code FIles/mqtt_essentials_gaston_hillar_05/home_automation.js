/*
Book: MQTT Essentials
Chapter 5: Control home automation in JavaScript
Author: Gaston C. Hillar - Twitter.com/gastonhillar
Publisher: Packt Publishing Ltd. - http://www.packtpub.com
*/

var APP = APP || {};
APP.HomeAutomation = APP.HomeAutomation || {};
APP.HomeAutomation.Manager = {
    ledCommandBaseTopic: "home/leds/",
    ledResultBaseTopic: "home/results/leds/",

    // Replace with the host name for the MQTT Server
    host: "localhost",
    // Replace with the port number for MQTT over WebSockets the MQTT Server
    port: 9001,
    clientId: "home-web-" + clientId: "home-web-" + Math.random().toString(16).substr(2, 8),

    mqttConnectOptions: {
        timeout: 3,
        // I want to use MQTT Version 3.1.1
        mqttVersion: 4,
        mqttVersionExplicit: true,
        // For TLS security, you just need to set useSSL to True
        // and install the necessary certificates
        // useSSL: true,
        useSSL: false,
        cleanSession: true
    },

    updateLed: function(ledId, jscolor) {
        console.log('I will send ' + jscolor + ' to LED #' + ledId);
        var payload = {
            "Color": jscolor.toString()
        };

        payloadString = JSON.stringify(payload);
        message = new Paho.MQTT.Message(payloadString);
        message.destinationName = this.ledCommandBaseTopic + ledId;
        message.qos = 0;

        this.client.send(message);
    },

    onConnectionLost: function(responseObject) {
        if (responseObject.errorCode !== 0)
            console.log("onConnectionLost: " + responseObject.errorMessage);
    },

    onMessageArrived: function(message) {
        console.log("Message arrived for topic: " + message.destinationName + ", with the following payload: " + message.payloadString);
        if (!message.destinationName.startsWith(APP.HomeAutomation.Manager.ledResultBaseTopic)) {
            return;
        }
        var ledNumber = message.destinationName.replace(APP.HomeAutomation.Manager.ledResultBaseTopic, "");
        var payload = JSON.parse(message.payloadString);
        if (ledNumber && payload.Color) {
            // Update the status for the LED that has successfully changed its color
            var statusLedDiv = document.getElementById("status-message-led-" + ledNumber);
            //statusLedDiv.style.backgroundColor = "#" + payload.Color;
            statusLedDiv.textContent = "LED's color set to #" + payload.Color;
            var statusLedCircle = document.getElementById("status-circle-led-" + ledNumber);
            statusLedCircle.style.fill = "#" + payload.Color;
    },

    onMessageDelivered: function(message) {
    },

    onConnectSuccess: function(invocationContext) {
        // Update the status text
        document.getElementById("status").textContent = "Connected with the MQTT Server";
        // Now, subscribe to home/results/leds/1, home/results/leds/2 and
        // home/results/leds/3
        var client = invocationContext.invocationContext.client;
        for (var i = 1; i < 4; i++) {
            client.subscribe("home/results/leds/" + i);
        }
    },

    connect: function() {
        this.client = new Paho.MQTT.Client(this.host, this.port, this.clientId);
        this.client.onConnectionLost = this.onConnectionLost;
        this.client.onMessageArrived = this.onMessageArrived;
        this.client.onMessageDelivered = this.onMessageDelivered;
        // I want to receive the client in the onSuccess and onFailure callbacks
        // Hence, I save a reference to this.client in invocationContext
        this.mqttConnectOptions.invocationContext = {
          client: this.client 
        };
        this.mqttConnectOptions.onSuccess = this.onConnectSuccess;
        this.mqttConnectOptions.onFailure = function (message) {
          console.log("Connection has failed: " + message);
        }
        this.client.connect(this.mqttConnectOptions);
    }
};

