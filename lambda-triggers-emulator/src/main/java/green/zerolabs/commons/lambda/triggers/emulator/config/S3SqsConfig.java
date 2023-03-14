package green.zerolabs.commons.lambda.triggers.emulator.config;

import io.smallrye.config.ConfigMapping;

/***
 * Created by Triphon Penakov 2023-02-13
 */
@ConfigMapping(prefix = "s3.sqs")
public interface S3SqsConfig extends SqsConfig {}
