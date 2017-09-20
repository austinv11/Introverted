package com.austinv11.introverted.test;

import com.austinv11.introverted.client.IntrovertedClient;
import com.austinv11.introverted.server.IntrovertedServer;
import org.junit.Test;

import java.io.IOException;

public class PingPongTest {

    @Test
    public void test() throws IOException {
        //Use introverted over tcp to ensure cross platform compat
        IntrovertedServer server = new IntrovertedServer(1337);
        IntrovertedClient client = new IntrovertedClient(1337);

        long ping1 = client.pollPing(), ping2 = client.pollPing(), ping3 = client.pollPing();
        long ping4 = server.pollPing(), ping5 = server.pollPing(), ping6 = server.pollPing();
        System.out.println((ping1 + ping2 + ping3) / 3); //Note this is likely to be a little longer than the second time because of java class loading
        System.out.println((ping4 + ping5 + ping6) / 3);

        server.close();
        client.close();
    }
}
