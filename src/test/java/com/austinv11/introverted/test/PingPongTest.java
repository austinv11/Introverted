package com.austinv11.introverted.test;

import com.austinv11.introverted.client.IntrovertedClient;
import com.austinv11.introverted.server.IntrovertedServer;
import org.junit.Test;

public class PingPongTest {

    @Test
    public void test() {
        IntrovertedServer server = new IntrovertedServer(1337);
        IntrovertedClient client = new IntrovertedClient(1337);

        long ping1 = client.pollPing(), ping2 = client.pollPing(), ping3 = client.pollPing();
        long ping4 = server.pollPing(), ping5 = server.pollPing(), ping6 = server.pollPing();
        System.out.println((ping1 + ping2 + ping3) / 3);
        System.out.println((ping4 + ping5 + ping6) / 3);
    }
}
