package io.github.shyrkaio.sample.events;

import io.github.shyrkaio.sample.Tom;

import io.fabric8.kubernetes.client.Watcher;

import io.javaoperatorsdk.operator.processing.event.DefaultEvent;

public class TomEvent extends DefaultEvent {

    private final Watcher.Action action;
    private final Tom instance;

    public TomEvent(
            Watcher.Action action, Tom resource,
            TomEventSource myEventSource) {

        super("", myEventSource);
        this.action = action;
        this.instance = resource;
    }

    public Watcher.Action getAction() {
        return action;
    }

    public String resourceUid() {
        return getTom().getMetadata().getUid();
    }

    @Override
    public String toString() {
        return "CustomResourceEvent{"
                + "action="
                + action
                + ", resource=[ name="
                + getTom().getMetadata().getName()
                + ", kind="
                + getTom().getKind()
                + ", apiVersion="
                + getTom().getApiVersion()
                + " ,resourceVersion="
                + getTom().getMetadata().getResourceVersion()
                + ", markedForDeletion: "
                + (getTom().getMetadata().getDeletionTimestamp() != null
                && !getTom().getMetadata().getDeletionTimestamp().isEmpty())
                + " ]"
                + '}';
    }

    public Tom getTom() {
        return instance;
    }
}
