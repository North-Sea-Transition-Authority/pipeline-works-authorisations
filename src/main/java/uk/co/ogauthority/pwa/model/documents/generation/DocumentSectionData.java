package uk.co.ogauthority.pwa.model.documents.generation;

import java.util.Map;

public class DocumentSectionData {

  private final String templatePath;

  private final Map<String, Object> templateModel;

  public DocumentSectionData(String templatePath, Map<String, Object> templateModel) {
    this.templatePath = templatePath;
    this.templateModel = templateModel;
  }

  public String getTemplatePath() {
    return templatePath + ".ftl";
  }

  public Map<String, Object> getTemplateModel() {
    return templateModel;
  }

}
