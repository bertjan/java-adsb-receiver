package nl.revolution.adsb.debug;

import nl.revolution.adsb.data.LoggingDataService;
import nl.revolution.adsb.decoder.Dump1090Source;
import nl.revolution.adsb.server.Config;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class AdsbDataLogger {

    private static final Logger LOG = Log.getLogger(AdsbDataLogger.class);

    public static void main(String[] args) throws Exception {
        Dump1090Source dataServer = new Dump1090Source(Config.DUMP1090_URL, new LoggingDataService());
        dataServer.start();

        LOG.info("Data logger started. Press enter to quit.");
        System.in.read();

        dataServer.stop();

        LOG.info("Stopped.");
    }
}

