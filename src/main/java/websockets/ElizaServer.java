package websockets;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import websockets.web.ElizaClientEndpoint;
import websockets.web.ElizaServerEndpoint;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElizaServer {
  private static final Logger LOGGER = Grizzly.logger(ElizaClient.class);
  public static final CountDownLatch LATCH = new CountDownLatch(1);

  public static void main(String[] args) {
    runClient();
  }

  private static void runClient() {
    ClientManager client = ClientManager.createClient();
    try {
      client.connectToServer(ElizaServerEndpoint.class, new URI("ws://localhost:8025/websockets/broker/doctor"));
      LATCH.await();
    } catch (DeploymentException | IOException | URISyntaxException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }
}
