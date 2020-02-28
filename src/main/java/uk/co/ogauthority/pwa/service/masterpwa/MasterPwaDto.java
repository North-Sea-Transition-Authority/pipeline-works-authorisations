package uk.co.ogauthority.pwa.service.masterpwa;

public class MasterPwaDto {

  private final String reference;
  private final int masterPwaId;

  public MasterPwaDto(String reference, int masterPwaId) {
    this.reference = reference;
    this.masterPwaId = masterPwaId;
  }

  public String getReference() {
    return reference;
  }

  public int getMasterPwaId() {
    return masterPwaId;
  }
}
