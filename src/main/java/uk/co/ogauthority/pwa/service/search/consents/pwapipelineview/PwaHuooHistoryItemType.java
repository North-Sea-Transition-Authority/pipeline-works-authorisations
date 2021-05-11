package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

/*
This class identifies what type of item is in a collection of versioned huoo data linked to a pipeline
  whether it be consent OR the pipelineDetail based HUOO data
 */

public enum PwaHuooHistoryItemType {

  PIPELINE_DETAIL_MIGRATED_HUOO("PIPELINE_DETAIL_ID_"),
  PWA_CONSENT("PWA_CONSENT_");

  private final String itemPrefix;

  PwaHuooHistoryItemType(String itemPrefix) {
    this.itemPrefix = itemPrefix;
  }

  public String getItemPrefix() {
    return itemPrefix;
  }
}
