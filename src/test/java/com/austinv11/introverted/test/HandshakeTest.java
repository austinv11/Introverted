package com.austinv11.introverted.test;

import com.austinv11.introverted.client.IntrovertedClient;
import com.austinv11.introverted.server.IntrovertedServer;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class HandshakeTest {

    @Test
    public void handshake() throws InterruptedException, IOException {
        IntrovertedServer server = new IntrovertedServer(1337);
        IntrovertedClient client = new IntrovertedClient(1337);
        assertTrue(!client.handshake().isPresent());
        client.cleanlyClose(0);
    }
}
