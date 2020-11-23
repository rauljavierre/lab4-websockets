package websockets.web;

import org.glassfish.grizzly.Grizzly;
import websockets.service.Eliza;

import javax.websocket.*;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/broker/{id}")
public class ElizaBrokerEndpoint {

  private static final Logger LOGGER = Grizzly.logger(ElizaBrokerEndpoint.class);

  private static Session clientSession = null;
  private static Session doctorSession = null;

  @OnOpen
  public void onOpen(Session session, @PathParam("id") String id) {
    LOGGER.info("[BROKER] New session ... " + session.getId() + " | " + id);

    if (id.equals("client")) {
      clientSession = session;

      if (doctorSession != null) {
        doctorSession.getAsyncRemote().sendText("WAKE UP, SERVER!!!!!!!!!");
      }
    }
    else {  // "doctor"
      doctorSession = session;
      doctorSession.getAsyncRemote().sendText("WAKE UP, SERVER!!!!!!!!!");
    }
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    LOGGER.info("[BROKER] Message ... " + message);

    if (session == clientSession && doctorSession != null) {
      doctorSession.getAsyncRemote().sendText(message);
    }
    else if (session == doctorSession && clientSession != null){
      clientSession.getAsyncRemote().sendText(message);
    }
  }

  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    LOGGER.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
  }

  @OnError
  public void onError(Session session, Throwable errorReason) {
    LOGGER.log(Level.SEVERE,
            String.format("Session %s closed because of %s", session.getId(), errorReason.getClass().getName()),
            errorReason);
  }
}
