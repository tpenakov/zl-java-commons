package green.zerolabs.commons.core.model.graphql.generated;

import java.util.List;

// Autogenerated. Do not modify. Will be overridden. Generated by green.zerolabs.gqgen.Generator$TypeGenerator
@io.quarkus.runtime.annotations.RegisterForReflection
@lombok.Data
@lombok.NoArgsConstructor
public class GetSingleCertificateDataRs implements java.io.Serializable,ErrorResponse  {
    CertificateDataRs data;
    Boolean isError;
    List<String> errors;
}
