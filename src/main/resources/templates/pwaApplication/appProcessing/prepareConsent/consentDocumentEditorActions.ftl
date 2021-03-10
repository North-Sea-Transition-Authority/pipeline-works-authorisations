<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="consentDocumentUrlFactory" type="uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentDocumentUrlFactory" -->
<#-- @ftlvariable name="userProcessingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission>" -->

<#macro actions userProcessingPermissions consentDocumentUrlFactory>

    <#assign consentReviewPermission = userProcessingPermissions?seq_contains("CONSENT_REVIEW") />
    <#assign sendForApprovalPermission = userProcessingPermissions?seq_contains("SEND_CONSENT_FOR_APPROVAL") />

    <#if consentReviewPermission>
        <@fdsAction.link linkText="Issue consent" linkUrl=springUrl(consentDocumentUrlFactory.issueConsentUrl) linkClass="govuk-button govuk-button--green" />
        <@fdsAction.link linkText="Return to case officer" linkUrl=springUrl(consentDocumentUrlFactory.returnToCaseOfficerUrl) linkClass="govuk-button govuk-button--blue" />
    </#if>

    <#if sendForApprovalPermission>
        <@fdsAction.link linkText="Send for approval" linkUrl=springUrl(consentDocumentUrlFactory.sendForApprovalUrl) linkClass="govuk-button govuk-button--green" />
    </#if>

    <@fdsAction.link
      linkText="Preview document"
      linkUrl=springUrl(consentDocumentUrlFactory.downloadUrl)
      linkClass="govuk-button govuk-button--" + consentReviewPermission?then("secondary", "blue") />

    <#if !consentReviewPermission>
        <@fdsAction.link linkText="Reload document" linkUrl=springUrl(consentDocumentUrlFactory.reloadDocumentUrl) linkClass="govuk-button govuk-button--secondary" />
    </#if>

</#macro>