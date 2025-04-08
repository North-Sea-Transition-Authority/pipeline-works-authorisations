package uk.co.ogauthority.pwa.model.enums.notify;

/**
 * Enumeration of templates stored in GOV.UK Notify.
 */
public enum NotifyTemplate {

  // Application workflow changes
  REVIEW_AND_SUBMIT_APPLICATION("43298e9d-ac29-4064-b321-f701f331162f", DomainType.PAD),
  APPLICATION_SUBMITTED("d21f4756-0c2c-418e-aca5-b92dc868d726", DomainType.PAD),
  APPLICATION_WITHDRAWN("86a75ea3-0172-4969-ae69-f511e8ca6fdd", DomainType.APP),
  OPTIONS_VARIATION_CLOSED_WITHOUT_CONSENT("55abcaa2-b407-4a12-b2a8-271604cba3be", DomainType.APP),

  CONSENT_REVIEW("e16a2e7b-f59b-4bcd-8ac2-6c70e9cf7e4d", DomainType.PAD),
  CONSENT_REVIEW_RETURNED("e1b1dcaf-19fb-4b7b-9418-18640db38cb0", DomainType.PAD),
  CONSENT_ISSUED("55fec21a-d891-40ac-afbd-a36895dd2e0c", DomainType.PAD),

  HOLDER_CHANGE_CONSENTED("4ed5029c-84da-44eb-81df-9a45e8b6499f", DomainType.APP),

  // Consent issued
  NEW_PWA_CONSENT_ISSUED_HOLDER("ef43b3fc-dafa-43cf-8af3-7608018f07c8", DomainType.PAD),
  NEW_PWA_CONSENT_ISSUED_NON_HOLDER("f1c89ec7-6f97-440b-8b26-fe374a5aac0f", DomainType.PAD),
  VARIATION_CONSENT_ISSUED_HOLDER("0fbc7e1a-973c-41ab-b879-113f98ff2c13", DomainType.PAD),
  VARIATION_CONSENT_ISSUED_NON_HOLDER("ee9913db-46e2-473c-965a-ace4003d9655", DomainType.PAD),
  HUOO_CONSENT_ISSUED_HOLDER("e08f6838-23fe-4beb-804f-7841b669b888", DomainType.PAD),
  HUOO_CONSENT_ISSUED_NON_HOLDER("858373c2-6a19-4ccb-92f6-90d74ee85d78", DomainType.PAD),
  DEPCON_CONSENT_ISSUED_HOLDER("d929c3b0-961c-4acf-91bb-6d6accd59786", DomainType.PAD),
  DEPCON_CONSENT_ISSUED_NON_HOLDER("21f7611c-6012-4d3e-bf0a-dfd62f816528", DomainType.PAD),

  // Update requests
  APPLICATION_UPDATE_REQUESTED("4cf29c12-9c3e-4007-9118-56f9612c2f78", DomainType.PAD),
  APPLICATION_UPDATE_RESPONDED("b42ac52d-ba14-4fd5-b95b-a1f91cf8fea1", DomainType.PAD),
  APPLICATION_UPDATE_ACCEPTED("c95d2a53-961a-4371-81ae-0e4915324f3a", DomainType.APP),

  // Assignment
  CASE_OFFICER_ASSIGNED("5a5e5838-5395-4129-97f0-0ed8d4b2dd40", DomainType.PAD),
  CASE_OFFICER_ASSIGNMENT_FAIL("a0bb276f-d953-45f2-a968-16dd39f8311c", DomainType.APP),
  APPLICATION_ASSIGNED_TO_YOU("26aea63f-b7e0-4ab6-aa4c-0c8805d13030", DomainType.PAD),

  // Consultations
  CONSULTATION_ASSIGNED_TO_YOU("ed1f6872-2bc3-4d04-809a-fdaff8f2c4d9", DomainType.APP),
  CONSULTATION_RESPONSE_RECEIVED("cf4f28d9-d015-448b-9fa0-efc4ea77beef", DomainType.APP),
  CONSULTATION_MULTI_RESPONSE_RECEIVED("d3eb3a18-654a-45db-9188-5a2aede59de7", DomainType.APP),
  CONSULTATION_REQUEST_RECEIVED("12896878-784a-4762-9a59-41ff22fb0c32", DomainType.APP),
  CONSULTATION_WITHDRAWN("faad92cf-e5ff-41a6-addb-249d5b3364e3", DomainType.APP),

  // Options approval
  APPLICATION_OPTIONS_APPROVED("0b0601ef-9b25-43e7-b6e7-42194e5c57ff", DomainType.APP),
  APPLICATION_OPTIONS_APPROVAL_DEADLINE_CHANGE("eb8070db-f265-4b0d-819c-7e460d95c319", DomainType.APP),

  // Notify callbacks
  EMAIL_DELIVERY_FAILED("f55d7ad7-2f25-444e-b207-a1f9d27155fe", DomainType.FAIL),

  // Public notice
  PUBLIC_NOTICE_APPROVAL_REQUEST("f95e8f73-a71a-4716-8b4a-9dd1a7d4697d", DomainType.APP),
  PUBLIC_NOTICE_APPROVED("560921df-3305-4236-9af1-54cec4fdb6d0", DomainType.APP),
  PUBLIC_NOTICE_REJECTED("aa948e0a-e0e2-439d-8bd5-faca893be3b9", DomainType.APP),
  PUBLIC_NOTICE_DOCUMENT_REVIEW_REQUEST("0adbefc5-d862-427e-8e83-41dc1c623b06", DomainType.APP),
  PUBLIC_NOTICE_UPDATE_REQUESTED("3011cb7b-0d9a-494f-b6ad-d3fe88facf9b", DomainType.APP),
  PUBLIC_NOTICE_WITHDRAWN("3eec4450-cc55-4d15-88fa-a796374a1d06", DomainType.APP),
  PUBLIC_NOTICE_PUBLICATION("e040c485-a13b-4ba3-b495-75babbd39013", DomainType.APP),
  PUBLIC_NOTICE_PUBLICATION_UPDATE("7adea559-fe82-460b-8241-ff8726c0bf34", DomainType.APP),

  // Application payments
  APPLICATION_PAYMENT_REQUEST_ISSUED("61294b50-0ddd-4f01-83b2-05efb1cff4b7", DomainType.APP),
  APPLICATION_PAYMENT_REQUEST_CANCELLED("a0d744fb-4380-485c-8c20-6e98941964eb", DomainType.APP),

  //As built
  AS_BUILT_NOTIFICATION_NOT_PER_CONSENT(
      "e745c23f-74c3-4417-ab9c-d439a83fe38a", DomainType.AS_BUILT_GROUP),
  AS_BUILT_DEADLINE_UPCOMING("49a86c33-0c03-4cee-9cb9-208520c672ee", DomainType.AS_BUILT_GROUP),
  AS_BUILT_DEADLINE_PASSED("2f543b5e-1048-4812-9679-fe2b958eb30f", DomainType.AS_BUILT_GROUP),

  //Feedback
  FEEDBACK_FAILED_TO_SEND("29bfa17c-62e7-42a0-9545-6569a7dc4c27", DomainType.FAIL),

  // Team Management
  ADDED_MEMBER_TO_TEAM("52505864-cd5b-44f3-a3ba-9fa0346e9685", DomainType.TEAM);

  private final String templateId;

  private final DomainType domainType;

  NotifyTemplate(
      String templateId,
      DomainType domainType
  ) {
    this.templateId = templateId;
    this.domainType = domainType;
  }

  public String getTemplateId() {
    return templateId;
  }

  public DomainType getDomainType() {
    return domainType;
  }
}
