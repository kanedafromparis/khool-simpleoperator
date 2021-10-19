package io.github.shyrkaio.sample.events;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.github.shyrkaio.sample.TomController;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javaoperatorsdk.operator.processing.KubernetesResourceUtils.getUID;
import static io.javaoperatorsdk.operator.processing.KubernetesResourceUtils.getVersion;


public class DeployEventSource extends AbstractEventSource implements Watcher<Deployment> {


    private static final Logger log = LoggerFactory.getLogger(DeployEventSource.class);

    private final KubernetesClient client;

    public static DeployEventSource createAndRegisterWatch(KubernetesClient client) {
        DeployEventSource operatorEventSource = new DeployEventSource(client);
        operatorEventSource.registerWatch();
        return operatorEventSource;
    }

    private DeployEventSource(KubernetesClient client) {
        this.client = client;
    }

    private void registerWatch() {
        client.apps().deployments().inAnyNamespace()
                .withLabel("app.kubernetes.io/created-by", TomController.class.getCanonicalName()).watch(this);
    }

    @Override
    public void eventReceived(Action action, Deployment instance) {
        log.info("Event received for action: {}, Deployment: {}", action.name(), instance.getMetadata().getName());

        if (action == Action.ERROR) {
            log.warn(
                    "Skipping {} event for custom resource uid: {}, version: {}",
                    action,
                    getUID(instance),
                    getVersion(instance));
            return;
        }
        eventHandler.handleEvent(new DeployEvent(action, instance, this));
    }

    @Override
    public void onClose(WatcherException e) {
        if (e == null) {
            return;
        }
        if (e.isHttpGone()) {
            log.warn("Received error for watch, will try to reconnect.", e);
            registerWatch();
        } else {
            // Note that this should not happen normally, since fabric8 client handles reconnect.
            // In case it tries to reconnect this method is not called.
            log.error("Unexpected error happened with watch. Will exit.", e);
            System.exit(1);
        }
    }

}