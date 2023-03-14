package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.ZlCertificate;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

import java.util.List;

/*
 * Created by triphon 4.05.22 г.
 */
public interface ZlCertificateJsonService extends ZlJsonUploadService<ZlCertificate> {
  String COUNTRY_REGION_SPLIT = "-";

  Uni<Tuple2<ZlCertificate, ZlCertificate>> split(
      final ZlCertificate.IndexedCertificate newCertificate, final ZlCertificate oldCertificate);

  Uni<List<ZlCertificate>> mintInDb(final List<ZlCertificate> zlCertificates);

  Uni<ZlCertificate> merge(final List<ZlCertificate> zlCertificates);

  Uni<ZlCertificate> sold(final ZlCertificate zlCertificate);

  Uni<ZlCertificate> claim(final ZlCertificate zlCertificate);
}
