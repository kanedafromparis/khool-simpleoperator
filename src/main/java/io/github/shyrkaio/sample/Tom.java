package io.github.shyrkaio.sample;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("io.github.shyrkaio.sample")
@Version("v1")
@Kind("Tom")
@Plural("toms")

public class Tom extends CustomResource<TomSpec, TomStatus> implements Namespaced {

}