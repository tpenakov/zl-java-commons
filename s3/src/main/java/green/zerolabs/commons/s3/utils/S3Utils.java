package green.zerolabs.commons.s3.utils;

import green.zerolabs.commons.core.model.S3EventMessage;
import green.zerolabs.commons.core.utils.JsonUtils;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import java.text.MessageFormat;
import java.util.Optional;

/*
 * Created by triphon 25.04.22 г.
 */
@Getter
@Slf4j
public class S3Utils {
  private final S3AsyncClient s3AsyncClient;
  private final JsonUtils jsonUtils;

  public S3Utils(final S3AsyncClient s3AsyncClient, final JsonUtils jsonUtils) {
    this.s3AsyncClient = s3AsyncClient;
    this.jsonUtils = jsonUtils;
  }

  public Uni<ResponseBytes<GetObjectResponse>> readObject(final S3EventMessage.Record record) {
    return readObject(record.getS3().getBucket().getName(), record.getS3().getObject().getKey());
  }

  public Uni<byte[]> readObjectAsBytes(final String bucket, final String key) {
    return readObject(bucket, key)
        .onItem()
        .ifNotNull()
        .transform(response -> response.asByteArray());
  }

  public Uni<ResponseBytes<GetObjectResponse>> readObject(final String bucket, final String key) {
    return Uni.createFrom()
        .completionStage(
            getS3AsyncClient()
                .getObject(
                    builder -> builder.bucket(bucket).key(key),
                    AsyncResponseTransformer.toBytes()));
  }

  public Uni<Boolean> putObject(final S3EventMessage.Record record, final String body) {
    final String bucket = record.getS3().getBucket().getName();
    final String key = record.getS3().getObject().getKey();
    return putObject(bucket, key, body);
  }

  public Uni<Boolean> putObject(final String bucket, final String key, final String body) {
    return putObject(bucket, key, AsyncRequestBody.fromString(body));
  }

  public Uni<Boolean> putObject(final String bucket, final String key, final byte[] body) {
    return putObject(bucket, key, AsyncRequestBody.fromBytes(body));
  }

  public Uni<Boolean> putObject(
      final String bucket, final String key, final AsyncRequestBody body) {
    return Uni.createFrom()
        .completionStage(
            getS3AsyncClient().putObject(builder -> builder.bucket(bucket).key(key), body))
        .map(response -> true)
        .onFailure()
        .recoverWithUni(
            throwable -> {
              log.error(
                  MessageFormat.format(
                      "Unable to store record. bucket={0}, key={1}, body={2}", bucket, key, body),
                  throwable);
              return Uni.createFrom().item(false);
            });
  }

  public Uni<Boolean> readOnly(final String bucket, final String key) {
    return Uni.createFrom()
        .completionStage(
            getS3AsyncClient()
                .putObjectAcl(
                    builder -> builder.acl(ObjectCannedACL.PUBLIC_READ).key(key).bucket(bucket)))
        .map(response -> true)
        .onFailure()
        .recoverWithUni(
            throwable -> {
              log.error(
                  MessageFormat.format(
                      "Unable make object read-only. bucket={0}, key={1}", bucket, key),
                  throwable);
              return Uni.createFrom().item(false);
            });
  }

  public static Optional<S3EventMessage.Record.S3> getS3Record(final S3EventMessage.Record input) {
    return Optional.ofNullable(input).map(S3EventMessage.Record::getS3);
  }

  public static Optional<S3EventMessage.Record.S3.Object> getS3RecordObject(
          final S3EventMessage.Record input) {
    return getS3Record(input).map(S3EventMessage.Record.S3::getObject);
  }

  public static Optional<String> getS3RecordObjectKey(final S3EventMessage.Record input) {
    return getS3RecordObject(input).map(S3EventMessage.Record.S3.Object::getKey);
  }
}
