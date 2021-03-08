package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

import java.util.Arrays;
import java.util.List;

public enum PublicNoticeRequestReason {

  ALL_CONSULTEES_CONTENT("All consultees content and ready to go"),
  CONSULTEES_NOT_ALL_CONTENT("Consultees not all content, consultation still running but needs to meet deadline");

  private final String reasonText;

  PublicNoticeRequestReason(String reasonText) {
    this.reasonText = reasonText;
  }

  public String getReasonText() {
    return reasonText;
  }

  public static List<PublicNoticeRequestReason> asList() {
    return Arrays.asList(PublicNoticeRequestReason.values());
  }
}
