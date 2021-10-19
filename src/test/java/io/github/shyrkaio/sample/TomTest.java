package io.github.shyrkaio.sample;

import org.junit.*;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;

import java.io.IOException;

public class TomTest {

    @Rule
    public KubernetesServer server;

    @Before
    public void setUp() {
        this.server = new KubernetesServer(true, true);
        this.server.before();
        //some mock setup

    }

    @After
    public void cleanUp() throws IOException {
        this.server.after();
        this.server.getMockServer().close();
    }


    @Test
    public void test() {
        Assert.assertSame("foo","foo");
    }

}