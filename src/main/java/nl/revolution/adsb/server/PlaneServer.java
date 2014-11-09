package nl.revolution.adsb.server;

import nl.revolution.adsb.api.WebServer;
import nl.revolution.adsb.data.MongoFlightDataService;
import nl.revolution.adsb.data.FlightDataService;
import nl.revolution.adsb.decoder.Dump1090Source;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class PlaneServer {

    private static final Logger LOG = Log.getLogger(PlaneServer.class);

    public enum Type { DATA, VIEW }

    public void run(PlaneServer.Type... type) throws Exception {

        FlightDataService flightDataService = createFlightDataService();

        Dump1090Source dataServer = null;
        if (ArrayUtils.contains(type, Type.DATA)) {
            dataServer = new Dump1090Source(Config.DUMP1090_URL, flightDataService);
            dataServer.start();
        }

        Server viewServer = null;
        if (ArrayUtils.contains(type, Type.VIEW)) {
            viewServer = new WebServer().createServer(Config.HTTP_PORT, flightDataService);
            viewServer.start();
        }

        LOG.info("Press enter to quit.");
        System.in.read();

        if (viewServer != null) {
            viewServer.stop();
        }

        if (dataServer != null) {
            dataServer.stop();
        }

        LOG.info("Stopped.");
    }

    private FlightDataService createFlightDataService() {
        return new MongoFlightDataService();
    }
}
