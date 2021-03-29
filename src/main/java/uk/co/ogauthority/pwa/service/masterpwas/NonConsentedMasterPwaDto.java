package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.Objects;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public final class NonConsentedMasterPwaDto {

  private final int masterPwaId;
  private final int masterPwaDetailId;
  private final String reference;

  private final int sourcePwaApplicationId;

  private final int holderPwaApplicationDetailId;
  private final SourceApplicationCategory sourceApplicationCategory;

  public NonConsentedMasterPwaDto(int masterPwaId,
                                  int masterPwaDetailId,
                                  String reference,
                                  int sourcePwaApplicationId,
                                  int holderPwaApplicationDetailId,
                                  PwaApplicationStatus pwaApplicationStatus) {
    this.masterPwaId = masterPwaId;
    this.masterPwaDetailId = masterPwaDetailId;
    this.reference = reference;
    this.sourcePwaApplicationId = sourcePwaApplicationId;
    this.holderPwaApplicationDetailId = holderPwaApplicationDetailId;
    this.sourceApplicationCategory = pwaApplicationStatus.equals(PwaApplicationStatus.DRAFT)
        ? SourceApplicationCategory.FIRST_DRAFT_APPLICATION
        : SourceApplicationCategory.SUBMITTED_APPLICATION;
  }

  public int getMasterPwaId() {
    return masterPwaId;
  }

  public int getMasterPwaDetailId() {
    return masterPwaDetailId;
  }

  public String getReference() {
    return reference;
  }

  public int getSourcePwaApplicationId() {
    return sourcePwaApplicationId;
  }

  public int getHolderPwaApplicationDetailId() {
    return holderPwaApplicationDetailId;
  }

  public SourceApplicationCategory getSourceApplicationCategory() {
    return sourceApplicationCategory;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NonConsentedMasterPwaDto that = (NonConsentedMasterPwaDto) o;
    return masterPwaId == that.masterPwaId
        && masterPwaDetailId == that.masterPwaDetailId
        && sourcePwaApplicationId == that.sourcePwaApplicationId
        && holderPwaApplicationDetailId == that.holderPwaApplicationDetailId
        && Objects.equals(reference, that.reference)
        && sourceApplicationCategory == that.sourceApplicationCategory;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        masterPwaId,
        masterPwaDetailId,
        reference,
        sourcePwaApplicationId,
        holderPwaApplicationDetailId,
        sourceApplicationCategory
    );
  }

  public enum SourceApplicationCategory {
    SUBMITTED_APPLICATION, FIRST_DRAFT_APPLICATION
  }

}
