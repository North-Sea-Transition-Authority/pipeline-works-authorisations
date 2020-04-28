package uk.co.ogauthority.pwa.model.search;

public interface SearchSelectable {

  static String FREE_TEXT_PREFIX = "FT_";

  String getSelectionId();

  String getSelectionText();

}
