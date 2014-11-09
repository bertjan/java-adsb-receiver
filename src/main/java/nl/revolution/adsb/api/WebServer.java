package nl.revolution.adsb.api;

import nl.revolution.adsb.data.FlightDataService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlets.gzip.GzipHandler;

public class WebServer {

    public Server createServer(int httpPort, FlightDataService flightDataService) {
        ResourceHandler webResourceHandler = new ResourceHandler();
        webResourceHandler.setDirectoriesListed(false);
        webResourceHandler.setResourceBase("src/main/resources/web");
        webResourceHandler.setWelcomeFiles(new String[]{"views/index.html"});

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{webResourceHandler, new PlaneDataAPIHandler(flightDataService), new NotFoundHandler()});
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setHandler(handlers);

        Server webServer = new Server(httpPort);
        webServer.setHandler(gzipHandler);
        return webServer;
    }

}
