package nl.revolution.adsb.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoFlightDataService implements FlightDataService {

    private static final String COLLECTION_NAME = "flightData";
    private long tsLastMeasure;
    private long numberOfEventsReceived;

    public MongoFlightDataService() {
        // Fire up / check Mongo connection.
        getFlightData(System.currentTimeMillis());
        tsLastMeasure = System.currentTimeMillis();
    }

    @Override
    public void flightDataReceived(Map dataMap) {
        logFlightDataReceived();

        // Write data to Mongo.
        DBObject data = new BasicDBObject(dataMap);
        MongoConnector.getInstance().getDB().getCollection(COLLECTION_NAME).insert(data);
    }

    public List<JSONObject> getFlightData(Long minTimestamp) {
        DBObject filter = new BasicDBObject();
        if (minTimestamp != null) {
            filter.put("timestamp", new BasicDBObject("$gt", minTimestamp));
        }

        DBObject sort = new BasicDBObject("timestamp", 1);

        final List<DBObject> dbResults;
        try (DBCursor cursor = MongoConnector.getInstance()
                .getDB().getCollection(COLLECTION_NAME)
                .find(filter).sort(sort)) {
            dbResults = cursor.toArray();
            cursor.close();
        }

        if (dbResults == null) {
            return null;
        }

        return dbResults.stream().map(dbo -> new JSONObject(dbo.toMap())).collect(Collectors.toList());
    }


    private void logFlightDataReceived() {
        System.out.print(".");
        numberOfEventsReceived++;
        if (numberOfEventsReceived >= 50) {
            double diffSeconds = (System.currentTimeMillis() - tsLastMeasure) / 1000;
            if (diffSeconds > 0) {
                System.out.printf(" -> %.1f flight events per second.\r\n", (numberOfEventsReceived / diffSeconds));
            }
            tsLastMeasure = System.currentTimeMillis();
            numberOfEventsReceived = 0;
        }
    }

}
