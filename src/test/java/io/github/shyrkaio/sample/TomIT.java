package io.github.shyrkaio.sample;

import io.fabric8.kubernetes.client.*;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.config.runtime.DefaultConfigurationService;
import org.junit.*;


public class TomIT {


    final static String TEST_NS = "Tom-test";

    @Test
    public void test() {

        Config config = new ConfigBuilder().withNamespace(TEST_NS).build();
        KubernetesClient client = new DefaultKubernetesClient(config);

        Operator operator = new Operator(client, DefaultConfigurationService.instance());
        operator.register(new TomController(client));
        
        Assert.assertSame("foo","foo");
    }

}