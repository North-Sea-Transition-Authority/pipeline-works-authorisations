package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

import java.util.EnumSet;
import java.util.Set;

public enum PublicNoticeActions {

  NEW_DRAFT("Draft"),
  UPDATE_DRAFT("Update draft"),
  APPROVE("Update");


  private final String actionDisplayText;


  PublicNoticeActions(String actionDisplayText) {
    this.actionDisplayText = actionDisplayText;
  }

  public String getDisplayText() {
    return actionDisplayText;
  }

  public static Set<PublicNoticeActions> getExistingPublicNoticeActions() {
    return EnumSet.complementOf(EnumSet.of(PublicNoticeActions.NEW_DRAFT));
  }



}
