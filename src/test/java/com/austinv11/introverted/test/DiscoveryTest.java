package com.austinv11.introverted.test;

import com.austinv11.introverted.client.IntrovertedClient;
import com.austinv11.introverted.server.IntrovertedServer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DiscoveryTest {

    @Test(timeout = 60 * 1000)
    public void discover() { //This can take some time so we restrict it to search 16 ports
        IntrovertedServer server = new IntrovertedServer(15);
        List<Pair<String, Integer>> servers = IntrovertedClient.findTCPServers(1, 16);
        assertEquals(servers.size(), 1);
        assertEquals(servers.get(0).getLeft(), IntrovertedServer.JVM_LIGHT_PLATFORM);
    }
}
