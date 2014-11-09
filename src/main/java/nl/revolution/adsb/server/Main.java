package nl.revolution.adsb.server;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.HashSet;
import java.util.Set;

public class Main {

    private static final Logger LOG = Log.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Set<PlaneServer.Type> serverTypesToRun = new HashSet<>();

        if (ArrayUtils.contains(args, "data")) {
            serverTypesToRun.add(PlaneServer.Type.DATA);
        }

        if (ArrayUtils.contains(args, "view")) {
            serverTypesToRun.add(PlaneServer.Type.VIEW);
        }

        // No args: run both servers.
        if (args.length == 0) {
            serverTypesToRun.add(PlaneServer.Type.DATA);
            serverTypesToRun.add(PlaneServer.Type.VIEW);
        }

        LOG.info("Running server(s): " + serverTypesToRun);

        new PlaneServer().run(serverTypesToRun.toArray(new PlaneServer.Type[2]));
    }
}

