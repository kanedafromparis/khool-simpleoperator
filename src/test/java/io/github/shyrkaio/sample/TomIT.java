package io.github.shyrkaio.sample;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.*;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.config.runtime.DefaultConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class TomIT {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final static Logger stlog = LoggerFactory.getLogger(TomIT.class);

    final static String TEST_NS = "tomtest";

    static Config config;
    static KubernetesClient client;
    static Operator operator;
    static Namespace testNs;


    @BeforeClass
    public static void setUp() throws IOException {
        stlog.info("this test assume that you have a kubernetes server running");
        stlog.info("and a KUBECONFIG correctly configure");
        stlog.info("and a you have set SA and Roles Correctly (k8s:ressource k8s:apply)");


        /** create Operator first so finilizer would existe */
        TomIT.config = new ConfigBuilder().withNamespace(TEST_NS).build();
        TomIT.client = new DefaultKubernetesClient(config);
        TomIT.testNs = new NamespaceBuilder().withMetadata(
                new ObjectMetaBuilder().withName(TEST_NS).build()).build();

        if (StringUtils.isBlank(System.getenv("OPERATOR_INSTALLED_IN_TEST"))) {
            // on some CI platform like github (via github-action), the operator is installed
            // into a kubernetes cluster this allows to validate the cluster role
            TomIT.operator = new Operator(client, DefaultConfigurationService.instance());
            TomIT.operator.register(new TomController(client));
            TomIT.operator.start();
        }

        cleanUp();
        stlog.info("Creating test namespace {}", TEST_NS);
        TomIT.client.namespaces().createOrReplace(testNs);
    }

    @AfterClass
    public static void cleanUpOption() throws IOException {
        // for debugging purpose cleanup is only happening upfront
        //
    }

    private static void cleanUp() throws IOException {
        // We perform a pre-run cleanup instead of a post-run cleanup. This is to help with debugging test results
        // when running against a persistent cluster. The test namespace would stay after the test run so we can
        // check what's there, but it would be cleaned up during the next test run.
        client.resources(Tom.class).inNamespace(TEST_NS).delete();
        stlog.info("Cleanup: deleting test namespace {}", TEST_NS);
        TomIT.client.namespaces().delete(TomIT.testNs);
        await().atMost(5, MINUTES).until(() -> client.namespaces().withName(TEST_NS).get() == null);
    }




    @Test
    public void testDeployment() {
        Tom sample = new Tom();
        sample.setMetadata(new ObjectMetaBuilder().withName("tom").withNamespace(TEST_NS).build());
        sample.setSpec(new TomSpec());
        sample.getSpec().setVersion("10.0");

        var clientKub = client.resources(Tom.class);
        clientKub.inNamespace(TEST_NS).createOrReplace(sample);

        await().atMost(2,MINUTES).untilAsserted(() -> {
            Deployment dep = client.apps().deployments().inNamespace(TEST_NS).withName(sample.getMetadata().getName()).get();
            assertThat(dep, is(notNullValue()));
        });

    }

}