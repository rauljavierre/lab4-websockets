package websockets;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.tyrus.server.Server;
import websockets.web.ElizaBrokerEndpoint;
import websockets.web.ElizaServerEndpoint;

import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElizaBroker {
  private static final Logger LOGGER = Grizzly.logger(ElizaBroker.class);

  public static void main(String[] args) {
    runServer();
  }

  private static void runServer() {
    Server server = new Server("localhost", 8025, "/websockets", new HashMap<>(),
            ElizaBrokerEndpoint.class);

    try (Scanner s = new Scanner(System.in)) {
      server.start();
      LOGGER.info("Press 's' to shutdown now the broker...");
      while (!s.hasNext("s")) ;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    } finally {
      server.stop();
      LOGGER.info("Server stopped");
    }
  }
}
