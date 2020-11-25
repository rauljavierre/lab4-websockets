package websockets.web;

import org.glassfish.grizzly.Grizzly;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/broker/{id}")
public class ElizaBrokerEndpoint {

  private static final Logger LOGGER = Grizzly.logger(ElizaBrokerEndpoint.class);

  private static final Map<URI, Session> sessions = new HashMap<>();

  @OnOpen
  public void onOpen(Session session) throws URISyntaxException, InterruptedException {
    if (!URIexists(session.getRequestURI())) {
      LOGGER.info("[BROKER] New session URI ... " + session.getRequestURI());
      session.getUserProperties().put("pendingMessage", "WAKE UP, DOCTOR!!!!!!!!!");
      sessions.put(session.getRequestURI(), session);
      if (doctorExists()) {
        sendPendingMessages();
      }
    }
  }

  @OnMessage
  public void onMessage(String message, Session session) throws URISyntaxException {
    LOGGER.info("[BROKER] Message ... " + message);
    if(session.getRequestURI().toString().equals("/websockets/broker/doctor")){  // It is a doctor message
      URI clientURI = new URI(message.split(";")[0]);
      String decodedMessage = message.split(";")[1];
      getSession(clientURI).getAsyncRemote().sendText(decodedMessage);
    }
    else {                                                                      // It is a client message
      if (doctorExists()) {
        getDoctor().getAsyncRemote().sendText(encodeClientMessage(session.getRequestURI(), message));
        sendPendingMessages();
      }
      else {
        session.getUserProperties().put("pendingMessage", message);
        sessions.put(session.getRequestURI(), session);
      }
    }
  }

  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    LOGGER.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    sessions.put(session.getRequestURI(), null);
  }

  @OnError
  public void onError(Session session, Throwable errorReason) {
    LOGGER.log(Level.SEVERE, String.format("Session %s closed because of %s", session.getId(), errorReason.getClass().getName()), errorReason);
    sessions.put(session.getRequestURI(), null);
  }

  private boolean URIexists(URI uri) {
    return sessions.get(uri) != null;
  }

  private boolean doctorExists() throws URISyntaxException {
    return URIexists(new URI("/websockets/broker/doctor"));
  }

  private Session getSession(URI uri) {
    return sessions.get(uri);
  }

  private Session getDoctor() throws URISyntaxException {
    return getSession(new URI("/websockets/broker/doctor"));
  }

  // I'm so tired -> /websockets/broker/client;I'm so tired
  private String encodeClientMessage(URI clientURI, String message) {
    return clientURI.toString() + ";" + message;
  }

  private void sendPendingMessages() throws URISyntaxException {
    for (Map.Entry<URI, Session> uriSessionEntry : sessions.entrySet()) {
      Session session = uriSessionEntry.getValue();

      if (session != null) {
        String pendingMessage = (String) session.getUserProperties().getOrDefault("pendingMessage", null);
        uriSessionEntry.getValue().getUserProperties().put("pendingMessage", null);

        if (pendingMessage != null && !pendingMessage.startsWith("/websockets/broker/doctor")) {
          getDoctor().getAsyncRemote().sendText(encodeClientMessage(uriSessionEntry.getValue().getRequestURI(), pendingMessage));
        }
      }
    }
  }
}
