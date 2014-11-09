package nl.revolution.adsb.data;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public class LoggingDataService implements FlightDataService {

    private static final Logger LOG = Log.getLogger(LoggingDataService.class);

    @Override
    public void flightDataReceived(Map data) {
        LOG.info(data.toString());
    }

    @Override
    public List<JSONObject> getFlightData(Long minTimestamp) {
        throw new NotImplementedException("Logging only");
    }
}
