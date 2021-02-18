package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

import java.util.EnumSet;
import java.util.Set;

public enum PublicNoticeAction {

  NEW_DRAFT("Draft"),
  UPDATE_DRAFT("Update draft"),
  APPROVE("Update");


  private final String actionDisplayText;


  PublicNoticeAction(String actionDisplayText) {
    this.actionDisplayText = actionDisplayText;
  }

  public String getDisplayText() {
    return actionDisplayText;
  }

  public static Set<PublicNoticeAction> getExistingPublicNoticeActions() {
    return EnumSet.complementOf(EnumSet.of(PublicNoticeAction.NEW_DRAFT));
  }



}
