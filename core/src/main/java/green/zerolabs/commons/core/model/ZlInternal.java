package green.zerolabs.commons.core.model;

import java.io.Serializable;

/*
 * Created by Triphon Penakov 2022-09-01
 */
public interface ZlInternal extends Serializable {

  String INTERNAL = "internal";

  ZlInternalData getInternal();

  void setInternal(ZlInternalData value);
}
