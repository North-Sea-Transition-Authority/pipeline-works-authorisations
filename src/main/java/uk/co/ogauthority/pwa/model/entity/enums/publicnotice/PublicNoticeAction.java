package uk.co.ogauthority.pwa.model.entity.enums.publicnotice;

import java.util.EnumSet;
import java.util.Set;

public enum PublicNoticeAction {

  NEW_DRAFT("Draft", "public notice"),
  UPDATE_DRAFT("Update draft", "public notice"),
  APPROVE("Review", "public notice"),
  UPDATE_DOCUMENT("Update", "public notice document"),
  REQUEST_DOCUMENT_UPDATE("Request update", "to public notice"),
  FINALISE("Finalise", "public notice"),
  UPDATE_DATES("Update publication dates", "of public notice"),
  WITHDRAW("Withdraw", "public notice"),
  DOWNLOAD("Download", "public notice document");

  private final String actionDisplayText;
  private final String screenReaderText;

  PublicNoticeAction(String actionDisplayText, String screenReaderText) {
    this.actionDisplayText = actionDisplayText;
    this.screenReaderText = screenReaderText;
  }

  public String getDisplayText() {
    return actionDisplayText;
  }

  public String getScreenReaderText() {
    return screenReaderText;
  }

  public static Set<PublicNoticeAction> getExistingPublicNoticeActions() {
    return EnumSet.complementOf(EnumSet.of(PublicNoticeAction.NEW_DRAFT));
  }
}