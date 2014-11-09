package nl.revolution.adsb.api;

import nl.revolution.adsb.data.FlightDataService;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaneDataAPIHandler extends AbstractHandler {

    private static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=utf-8";
    private static final long DEFAULT_MAX_AGE_IN_MINUTES = 15;
    private final FlightDataService flightDataService;
    private static final Logger LOG = Log.getLogger(PlaneDataAPIHandler.class);

    public  PlaneDataAPIHandler(FlightDataService flightDataService) {
        this.flightDataService = flightDataService;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (!request.getRequestURI().equals("/api/plane")) {
            new NotFoundHandler().handle(target, baseRequest, request, response);
            return;
        }

        response.setContentType(CONTENT_TYPE_JSON_UTF8);
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String filter = request.getParameter("filter");
        OutputStream outputStream = response.getOutputStream();
        createAPIResponse(filter, outputStream);
        outputStream.flush();
        outputStream.close();
    }


    private void createAPIResponse(String filter, OutputStream out) throws IOException {
        Long maxHistoryInMinutes = determineMaxHistory(filter);
        Long minTimestamp = determineMinTimestamp(maxHistoryInMinutes);

        List<JSONObject> results = flightDataService.getFlightData(minTimestamp);
        Map<String, List<JSONObject>> flightDataMap = convertDBResultsToFlightDataMap(results);

        write(out, "{");
        writeHistory(out, maxHistoryInMinutes);
        write(out, "\"flights\":[");

        int index = 0;
        Long latestTimestamp = 0l;

        for (String flight : flightDataMap.keySet()) {
            List<Map> flightPositions = new ArrayList<>();
            latestTimestamp = processFlightData(latestTimestamp, flightPositions, flightDataMap.get(flight));

            if (!flightPositions.isEmpty()) {
                index++;
                if (index > 1) {
                    write(out, ",\n");
                }
                write(out, createPositionListForFlight(flight, flightPositions).toJSONString());
            }
        }

        write(out, "]");
        writeLastUpdatedTimestamp(out, latestTimestamp);
        write(out, "}");
    }

    private void writeHistory(OutputStream out, Long maxHistoryInMinutes) throws IOException {
        write(out, "\"history\":" + maxHistoryInMinutes + ",");
    }

    private JSONObject createPositionListForFlight(String flight, List<Map> coords) {
        Map<String, Object> flightCoord = new HashMap<>();
        flightCoord.put("flight", flight);
        flightCoord.put("positions", coords);
        flightCoord.put("heading", String.valueOf(coords.get(coords.size() - 1).get("heading")));
        return new JSONObject(flightCoord);
    }

    private Long processFlightData(Long latestTimestamp, List<Map> positions, List<JSONObject> flightData) {
        for (JSONObject result : flightData) {
            Map<String, String> position = new HashMap<>();

            position.put("lat", String.valueOf(result.get("latitude")));
            position.put("lon", String.valueOf(result.get("longitude")));
            position.put("heading", String.valueOf(result.get("heading")));
            positions.add(position);

            Long currentTimestamp = Long.valueOf(String.valueOf(result.get("timestamp")));
            if (currentTimestamp > latestTimestamp) {
                latestTimestamp = currentTimestamp;
            }

        }
        return latestTimestamp;
    }

    private Map<String, List<JSONObject>> convertDBResultsToFlightDataMap(List<JSONObject> results) {
        Map<String,List<JSONObject>> allFlightData = new HashMap<>();
        for (JSONObject result : results) {
            String flightId = String.valueOf(result.get("flight"));
            if (!allFlightData.containsKey(flightId)) {
                allFlightData.put(flightId, new ArrayList<>());
            }
            allFlightData.get(flightId).add(result);
        }
        return allFlightData;
    }

    private Long determineMinTimestamp(Long maxHistoryInMinutes) {
        Long maxAgeInMS = maxHistoryInMinutes * 60 * 1000;
        return System.currentTimeMillis() - maxAgeInMS;
    }

    private Long determineMaxHistory(String filter) {
        Long maxHistoryInMinutes = DEFAULT_MAX_AGE_IN_MINUTES;

        if (StringUtils.isNotEmpty(filter)) {
            try {
                maxHistoryInMinutes = Long.valueOf(filter);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid filter value '" + filter + "', using default value '" + maxHistoryInMinutes + "'.");
            }
        }
        return maxHistoryInMinutes;
    }

    private static void write(OutputStream out, String data) throws IOException {
        out.write(data.getBytes(Charsets.UTF_8));
    }


    private void writeLastUpdatedTimestamp(OutputStream out, Long latestTimestamp) throws IOException {
        String updated = String.valueOf(latestTimestamp);
        if ("0".equals(updated)) {
            updated = "<no data>";
        } else {
            updated = new DateTime(latestTimestamp).toString("dd/MM/yyyy HH:mm:ss");
        }
        write(out, ", \"updated\":\"" + updated + "\"");
    }



}



