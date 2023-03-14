package green.zerolabs.commons.s3.utils;

import green.zerolabs.commons.core.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/*
 * Created by triphon 16.06.22 г.
 */
@Getter
@Setter
@Slf4j
public class AllS3Utils {
  private final S3Utils s3Utils;
  private final JsonUtils jsonUtils;

  public AllS3Utils(final S3Utils s3Utils, final JsonUtils jsonUtils) {
    this.s3Utils = s3Utils;
    this.jsonUtils = jsonUtils;
  }
}
