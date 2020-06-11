package uk.co.ogauthority.pwa.service.masterpwas;

import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

public class MasterPwaView {

  private final int masterPwaId;
  private final String reference;

  private MasterPwaView(int masterPwaId, String reference) {
    this.masterPwaId = masterPwaId;
    this.reference = reference;
  }

  public static MasterPwaView from(MasterPwaDetail masterPwaDetail) {
    return new MasterPwaView(masterPwaDetail.getMasterPwaId(), masterPwaDetail.getReference());
  }

  public int getMasterPwaId() {
    return masterPwaId;
  }

  public String getReference() {
    return reference;
  }
}
