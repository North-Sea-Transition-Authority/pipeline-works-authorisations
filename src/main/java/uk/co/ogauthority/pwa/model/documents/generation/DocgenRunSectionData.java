package uk.co.ogauthority.pwa.model.documents.generation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;

@Entity
@Table(name = "docgen_run_section_data")
public class DocgenRunSectionData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "docgen_run_id")
  private DocgenRun docgenRun;

  private String templateName;

  private String htmlContent;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DocgenRun getDocgenRun() {
    return docgenRun;
  }

  public void setDocgenRun(DocgenRun docgenRun) {
    this.docgenRun = docgenRun;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getHtmlContent() {
    return htmlContent;
  }

  public void setHtmlContent(String htmlContent) {
    this.htmlContent = htmlContent;
  }
}
