java-adsb-receiver
==================

Simple ADS-B receiver in Java and JavaScript, using the Google Maps API.
Depends on 'dump1090' by Salvatore Sanfilippo: antirez/dump1090.
 
Background
---
See the following presentation:
* http://www.slideshare.net/BertJanSchrijver/jfall-2014-decoding-the-airspace-above-you-with-java-and-7-hardware
Difference between the setup in the presentation and this project: this project depends on dump1090
as input source for the ADS-B data.

Dependencies
---
- Java 8
- MongoDB
- Running dump1090 client on the network
- Compatible RTL-SDR device
- Preferably: tailor-made ADS-B antenna connected to the RTL-SDR device

RTL-SDR devices
---
I have used these two:
* http://www.aliexpress.com/item/RTL-SDR-FM-DAB-DVB-T-USB-2-0-Mini-Digital-TV-Stick-TV-Receiver-Portable/1670781135.html
* http://www.aliexpress.com/item/RTL-SDR-FM-DAB-DVB-T-USB-2-0-Mini-Digital-TV-Stick-DVBT-Dongle-SDR/1316276597.html

In general, when finding an RTL-SDR compatible device, just look for the best selling Realtek / RTL-SDR usb DVB-T stick.


Database setup
---
Install and start MongoDB. Database 'adsb' and collection 'flightData' will be created automatically.
Create the following index to speed up queries over lots of data:
```
db.flightData.ensureIndex({"timestamp": 1}, {"background": true});
```

Getting started
---
* Make sure a compatible RTL-SDR device is inserted and working correctly (use rtl_test to verify)
* Make sure MongoDB is installed and running
* Download and build dump1090 (https://github.com/antirez/dump1090)
* Start dump1090:
```
./dump1090 --interactive --net --aggressive --metric --interactive-ttl 5
```
* Verify that the dump1090 data API is reachable over HTTP (http://localhost:8080/data.json)
* Edit Config.java, set URL to dump1090 API accordingly
* Build and run this project:
    mvn clean verify
* The frontend should come up at http://localhost:1090


