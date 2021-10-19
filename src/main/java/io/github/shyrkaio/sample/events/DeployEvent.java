package io.github.shyrkaio.sample.events;


import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Watcher;
import io.javaoperatorsdk.operator.processing.event.DefaultEvent;

public class DeployEvent extends DefaultEvent {

    private final Watcher.Action action;
    private final Deployment deployment;

    public DeployEvent(
            Watcher.Action action, Deployment resource, DeployEventSource myEventSource) {
        super(resource.getMetadata().getOwnerReferences().get(0).getUid(), myEventSource);

        this.action = action;
        this.deployment = resource;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public String resourceUid() {
        return getWatchedRessource().getMetadata().getUid();
    }

    @Override
    public String toString() {
        return "DeploymentEvent{"
                + "action="
                + action
                + ", resource=[ name="
                + getWatchedRessource().getMetadata().getName()
                + ", kind="
                + getWatchedRessource().getKind()
                + ", apiVersion="
                + getWatchedRessource().getApiVersion()
                + " ,resourceVersion="
                + getWatchedRessource().getMetadata().getResourceVersion()
                + ", markedForDeletion: "
                + (getWatchedRessource().getMetadata().getDeletionTimestamp() != null
                && !getWatchedRessource().getMetadata().getDeletionTimestamp().isEmpty())
                + " ]"
                + '}';
    }

    public Deployment getWatchedRessource() {
        return deployment;
    }
}
