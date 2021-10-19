package io.github.shyrkaio.sample;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.github.shyrkaio.sample.events.TomEventSource;
import io.github.shyrkaio.sample.events.TomEvent;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.internal.CustomResourceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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

        if (instance.getMetadata() ==null){
          instance.setStatus(new TomStatus());
        }

        this.createOrUpdateDeployment(instance);
        log.info("status : {}", instance.getStatus());

        return UpdateControl.updateCustomResourceAndStatus(instance);
      }

  private void createOrUpdateDeployment(Tom instance) {
    String ns = instance.getMetadata().getNamespace();
    Deployment existingDeployment = kubernetesClient.apps().deployments()
            .inNamespace(ns).withName(instance.getMetadata().getName()).get();
    if (existingDeployment ==  null){
      existingDeployment = loadYaml(Deployment.class, "deployment.yaml");
      existingDeployment.getMetadata().setName(instance.getMetadata().getName());
      existingDeployment.getMetadata().setNamespace(ns);
      existingDeployment.getMetadata().getLabels().put("app.kubernetes.io/part-of", instance.getMetadata().getName());
      existingDeployment.getMetadata().getLabels().put("app.kubernetes.io/managed-by", instance.getMetadata().getName());
      existingDeployment.getMetadata().getLabels().put("app.kubernetes.io/created-by", TomController.class.getCanonicalName());

      // make sure label selector matches label (which has to be matched by service selector too)
      existingDeployment
              .getSpec()
              .getTemplate()
              .getMetadata()
              .getLabels()
              .put("app", instance.getMetadata().getName());
      existingDeployment
              .getSpec()
              .getSelector()
              .getMatchLabels()
              .put("app", instance.getMetadata().getName());

      OwnerReference ownerReference = existingDeployment.getMetadata().getOwnerReferences().get(0);
      ownerReference.setName(instance.getMetadata().getName());
      ownerReference.setUid(instance.getMetadata().getUid());

    }
    existingDeployment.getSpec().getTemplate()
            .getSpec().getContainers().get(0).setImage("tomcat:"+instance.getSpec().getVersion());
      existingDeployment.getSpec().setReplicas(1);
    kubernetesClient.apps().deployments().inNamespace(ns).createOrReplace(existingDeployment);
    log.info("Creating Deployment {} in {}", existingDeployment.getMetadata().getName(), ns);
  }

  private <T> T loadYaml(Class<T> clazz, String yaml) {
    try (InputStream is = getClass().getResourceAsStream(yaml)) {
      return Serialization.unmarshal(is, clazz);
    } catch (IOException ex) {
      throw new IllegalStateException("Cannot find yaml on classpath: " + yaml);
    }
  }

      @Override
      public DeleteControl deleteResource(Tom tom, Context<Tom> context) {        
        return DeleteControl.DEFAULT_DELETE;
      }

}