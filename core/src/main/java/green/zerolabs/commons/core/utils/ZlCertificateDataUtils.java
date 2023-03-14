package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.converter.InstantDeserializer;
import green.zerolabs.commons.core.model.ZlCertificateData;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Optional;

import static green.zerolabs.commons.core.utils.ConverterUtils.toAgreementDistributions;

/***
 * Created by Triphon Penakov 2022-12-14
 */
public class ZlCertificateDataUtils {
  public static void setAgreementDistributions(final ZlCertificateData result) {
    if (CollectionUtils.isNullOrEmpty(result.getAgreementDistributions())) {
      final String agreementDistributionsCsv = result.getAgreementDistributionsCsv();
      if (StringUtils.isBlank(agreementDistributionsCsv)) {
        result.setAgreementDistributions(null);
      } else {
        result.setAgreementDistributions(toAgreementDistributions(agreementDistributionsCsv));
      }
    }
    result.setAgreementDistributionsCsv(null);
    if (CollectionUtils.isNullOrEmpty(result.getAgreementDistributions())) {
      result.setAgreementDistributions(null);
    }
  }

  public static void fixDateStrings(final ZlCertificateData certificateData) {
    Optional.ofNullable(certificateData.getRedemptionDate())
        .filter(StringUtils::isNotBlank)
        .ifPresent(
            date ->
                certificateData.setRedemptionDate(InstantDeserializer.toInstant(date).toString()));
  }
}
