package uk.co.ogauthority.pwa.service.markdown;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MailMergeContainer {

  private Map<String, String> mailMergeFields;

  private Map<String, String> automaticMailMergeDataHtmlAttributeMap;
  private Map<String, String> manualMailMergeDataHtmlAttributeMap;

  public MailMergeContainer() {
    this.mailMergeFields = new HashMap<>();
    this.automaticMailMergeDataHtmlAttributeMap = new HashMap<>();
    this.manualMailMergeDataHtmlAttributeMap = new HashMap<>();
  }

  public Map<String, String> getMailMergeFields() {
    return mailMergeFields;
  }

  public void setMailMergeFields(Map<String, String> mailMergeFields) {
    this.mailMergeFields = mailMergeFields;
  }

  public Map<String, String> getAutomaticMailMergeDataHtmlAttributeMap() {
    return automaticMailMergeDataHtmlAttributeMap;
  }

  public void setAutomaticMailMergeDataHtmlAttributeMap(Map<String, String> automaticMailMergeDataHtmlAttributeMap) {
    this.automaticMailMergeDataHtmlAttributeMap = automaticMailMergeDataHtmlAttributeMap;
  }

  public Map<String, String> getManualMailMergeDataHtmlAttributeMap() {
    return manualMailMergeDataHtmlAttributeMap;
  }

  public void setManualMailMergeDataHtmlAttributeMap(Map<String, String> manualMailMergeDataHtmlAttributeMap) {
    this.manualMailMergeDataHtmlAttributeMap = manualMailMergeDataHtmlAttributeMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MailMergeContainer that = (MailMergeContainer) o;
    return Objects.equals(mailMergeFields, that.mailMergeFields) && Objects.equals(
        automaticMailMergeDataHtmlAttributeMap, that.automaticMailMergeDataHtmlAttributeMap) && Objects.equals(
        manualMailMergeDataHtmlAttributeMap, that.manualMailMergeDataHtmlAttributeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mailMergeFields, automaticMailMergeDataHtmlAttributeMap, manualMailMergeDataHtmlAttributeMap);
  }

}
