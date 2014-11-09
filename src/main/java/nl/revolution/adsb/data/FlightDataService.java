package nl.revolution.adsb.data;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public interface FlightDataService {

    public void flightDataReceived(Map data);

    public List<JSONObject> getFlightData(Long minTimestamp);
}
