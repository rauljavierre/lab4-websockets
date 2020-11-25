package websockets.web;

import console.TextDevice;
import console.TextDevices;
import org.glassfish.grizzly.Grizzly;
import websockets.ElizaClient;
import javax.websocket.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ClientEndpoint
public class ElizaClientEndpoint {

  private static final Logger LOGGER = Grizzly.logger(ElizaClientEndpoint.class);
  private TextDevice con = TextDevices.defaultTextDevice();

  @OnOpen
  public void onOpen(Session session) {}

  @OnMessage
  public void onMessage(String message, Session session) {
    if (message.equals("---")) {
      String line = con.readLine();
      session.getAsyncRemote().sendText(line);
    } else {
      con.printf("[Eliza] %s\n", message);
    }
  }

  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    con.printf("[Eliza] %s\n", closeReason.getReasonPhrase());
    LOGGER.info(String.format("Session %s closed because of %s", session.getId(), closeReason.getCloseCode()));
    ElizaClient.LATCH.countDown();
  }

  @OnError
  public void onError(Session session, Throwable errorReason) {
    LOGGER.log(Level.SEVERE, String.format("Session %s closed because of %s", session.getId(), errorReason.getClass().getName()), errorReason);
    ElizaClient.LATCH.countDown();
  }
}
