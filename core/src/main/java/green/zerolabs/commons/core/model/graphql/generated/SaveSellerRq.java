package green.zerolabs.commons.core.model.graphql.generated;

import org.eclipse.microprofile.graphql.NonNull;

// Autogenerated. Do not modify. Will be overridden. Generated by green.zerolabs.gqgen.Generator$TypeGenerator
@io.quarkus.runtime.annotations.RegisterForReflection
@lombok.Data
@lombok.NoArgsConstructor
public class SaveSellerRq implements java.io.Serializable  {
    String id;
    @NonNull String name;
    @NonNull String address;
    String contact;
    String website;
    String social;
    String blockchainAddress;
}
