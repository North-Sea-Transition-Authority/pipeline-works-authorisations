package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

/*
This class defines the common properties of consent and pipeline detail based huoo data
  allowing them to be collected as one type for processing
 */

public class PwaHuooHistoryItemVersion {

  private final Integer id;
  private final PwaHuooHistoryItemType pwaHuooHistoryItemType;
  private final Instant startTimestamp;
  private final String reference;
  private final Integer variationNumber;


  public PwaHuooHistoryItemVersion(Integer id,
                                   PwaHuooHistoryItemType pwaHuooHistoryItemType,
                                   Instant startTimestamp,
                                   String reference,
                                   Integer variationNumber) {
    this.id = id;
    this.pwaHuooHistoryItemType = pwaHuooHistoryItemType;
    this.startTimestamp = startTimestamp;
    this.reference = reference;
    this.variationNumber = variationNumber;
  }

  public static PwaHuooHistoryItemVersion fromPipelineDetail(PipelineDetail pipelineDetail) {
    return new PwaHuooHistoryItemVersion(
        pipelineDetail.getId(),
        PwaHuooHistoryItemType.PIPELINE_DETAIL_MIGRATED_HUOO,
        pipelineDetail.getStartTimestamp(),
        pipelineDetail.getPwaConsent().getReference(),
        null
    );
  }

  public static PwaHuooHistoryItemVersion fromConsent(PwaConsent pwaConsent) {
    return new PwaHuooHistoryItemVersion(
        pwaConsent.getId(),
        PwaHuooHistoryItemType.PWA_CONSENT,
        pwaConsent.getConsentInstant(),
        pwaConsent.getReference(),
        pwaConsent.getVariationNumber()
    );
  }



  public Integer getId() {
    return id;
  }

  public PwaHuooHistoryItemType getPwaHuooHistoryItemType() {
    return pwaHuooHistoryItemType;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public String getReference() {
    return reference;
  }

  public Integer getVariationNumber() {
    return variationNumber;
  }
}
