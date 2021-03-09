package uk.co.ogauthority.pwa.model.form.publicnotice;


public class PublicNoticeDocumentUpdateRequestForm {

  private String comments;

  public PublicNoticeDocumentUpdateRequestForm() {
    //default
  }

  public PublicNoticeDocumentUpdateRequestForm(String comments) {
    this.comments = comments;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }
}
