package green.zerolabs.commons.core.model.graphql.generated;

import java.util.List;
import org.eclipse.microprofile.graphql.NonNull;

// Autogenerated. Do not modify. Will be overridden. Generated by green.zerolabs.gqgen.Generator$TypeGenerator
@io.quarkus.runtime.annotations.RegisterForReflection
@lombok.Data
@lombok.NoArgsConstructor
public class W3BatchUpdateRq implements java.io.Serializable  {
    @NonNull String zlId;
    @NonNull String userId;
    String w3Id;
    List<String> certificateIds;
    String redemptionStatement;
    String storagePointer;
}