package uk.co.ogauthority.pwa.model.enums.consents;

import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public enum ConsentIssueEmail {

  NEW_PWA(
      NotifyTemplate.NEW_PWA_CONSENT_ISSUED_HOLDER,
      NotifyTemplate.NEW_PWA_CONSENT_ISSUED_NON_HOLDER,
      TemplateTextType.INITIAL_CONSENT_EMAIL_COVER_LETTER
  ),

  VARIATION(
      NotifyTemplate.VARIATION_CONSENT_ISSUED_HOLDER,
      NotifyTemplate.VARIATION_CONSENT_ISSUED_NON_HOLDER,
      TemplateTextType.VARIATION_CONSENT_EMAIL_COVER_LETTER
  ),

  HUOO(
      NotifyTemplate.HUOO_CONSENT_ISSUED_HOLDER,
      NotifyTemplate.HUOO_CONSENT_ISSUED_NON_HOLDER,
      TemplateTextType.HUOO_CONSENT_EMAIL_COVER_LETTER
  ),

  DEPCON(
      NotifyTemplate.DEPCON_CONSENT_ISSUED_HOLDER,
      NotifyTemplate.DEPCON_CONSENT_ISSUED_NON_HOLDER,
      TemplateTextType.DEPCON_CONSENT_EMAIL_COVER_LETTER
  );

  private final NotifyTemplate holderEmailTemplate;
  private final NotifyTemplate nonHolderEmailTemplate;
  private final TemplateTextType templateTextType;

  ConsentIssueEmail(NotifyTemplate holderEmailTemplate,
                    NotifyTemplate nonHolderEmailTemplate,
                    TemplateTextType templateTextType) {
    this.holderEmailTemplate = holderEmailTemplate;
    this.nonHolderEmailTemplate = nonHolderEmailTemplate;
    this.templateTextType = templateTextType;
  }

  public NotifyTemplate getHolderEmailTemplate() {
    return holderEmailTemplate;
  }

  public NotifyTemplate getNonHolderEmailTemplate() {
    return nonHolderEmailTemplate;
  }

  public TemplateTextType getTemplateTextType() {
    return templateTextType;
  }

}