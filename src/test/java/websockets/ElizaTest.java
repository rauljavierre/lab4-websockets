package websockets;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import websockets.web.ElizaBrokerEndpoint;
import websockets.web.ElizaClientEndpoint;
import websockets.web.ElizaServerEndpoint;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.*;
import static org.junit.Assert.assertEquals;

public class ElizaTest {

    private static final Logger LOGGER = Grizzly.logger(ElizaTest.class);

	private Server broker;

    @Before
	public void setup() throws DeploymentException, URISyntaxException, IOException {
		broker = new Server("localhost", 8025, "/websockets", new HashMap<>(), ElizaBrokerEndpoint.class);
		broker.start();

        ClientManager doctor = ClientManager.createClient();
        doctor.connectToServer(ElizaServerEndpoint.class, new URI("ws://localhost:8025/websockets/broker/doctor"));

	}

	@Test(timeout = 5000)
	public void onOpen() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
        Thread.sleep(500);
        CountDownLatch latch = new CountDownLatch(3);
        List<String> list = new ArrayList<>();
        ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();
        Session session = client.connectToServer(new Endpoint() {

            @Override
            public void onOpen(Session session, EndpointConfig config) {
                session.addMessageHandler(new ElizaOnOpenMessageHandler(list, latch));
            }

        }, configuration, new URI("ws://localhost:8025/websockets/broker/client"));
        session.getAsyncRemote().sendText("bye");
        latch.await();
        assertEquals(3, list.size());
        assertEquals("The doctor is in.", list.get(0));
	}

	@After
	public void close() {
		broker.stop();
	}

    @Test(timeout = 5000)
    public void onChat() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
        Thread.sleep(500);
        List<String> list = new ArrayList<>();
        ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();
        Session session = client.connectToServer(new Endpoint() {

            @Override
            public void onOpen(Session session, EndpointConfig config) {
                session.addMessageHandler(new ElizaOnOpenMessageHandler(list, null));
            }

        }, configuration, new URI("ws://localhost:8025/websockets/broker/client"));

        assertEquals(0, list.size());

        session.getAsyncRemote().sendText("Are you happy?");
        Thread.sleep(250);

        assertEquals(5, list.size());
        assertEquals("The doctor is in.", list.get(0));
        assertEquals("What's on your mind?", list.get(1));
        assertEquals("---", list.get(2));

        assertEquals("We were discussing you, not me.", list.get(3));
        assertEquals("---", list.get(4));
    }

    private static class ElizaOnOpenMessageHandler implements MessageHandler.Whole<String> {

        private final List<String> list;
        private final CountDownLatch latch;

        ElizaOnOpenMessageHandler(List<String> list, CountDownLatch latch) {
            this.list = list;
            this.latch = latch;
        }

        @Override
        public void onMessage(String message) {
            LOGGER.info(format("Client received \"%s\"", message));
            list.add(message);
            latch.countDown();
        }
    }
}
