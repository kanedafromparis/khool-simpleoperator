package io.github.shyrkaio.sample;

import io.github.shyrkaio.sample.events.TomEventSource;
import io.github.shyrkaio.sample.events.TomEvent;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.internal.CustomResourceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


@Controller
public class TomController implements ResourceController<Tom> {

    private static final Logger log = LoggerFactory.getLogger(TomController.class);


// add spec there
    private final KubernetesClient kubernetesClient;

    public TomController(KubernetesClient client) {
        this.kubernetesClient = client;
      }
    
      @Override
      public void init(EventSourceManager eventSourceManager) {
        log.info("Init");
        TomEventSource operatorEvenSource = TomEventSource.createAndRegisterWatch(kubernetesClient);
        eventSourceManager.registerEventSource("tom-event-source", operatorEvenSource);
      }
    
      @Override
      public UpdateControl<Tom> createOrUpdateResource(Tom instance, Context<Tom> context) {
        Optional<TomEvent> latestCREvent =
            context.getEvents().getLatestOfType(TomEvent.class);

        //doSomething(instance);

        log.info("status : {}", instance.getStatus());

        return UpdateControl.noUpdate();//UpdateControl.updateCustomResourceAndStatus(instance);
      }
    
      @Override
      public DeleteControl deleteResource(Tom tom, Context<Tom> context) {        
        return DeleteControl.DEFAULT_DELETE;
      }

}