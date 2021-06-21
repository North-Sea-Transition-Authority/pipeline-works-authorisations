<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="consentDocumentUrlProvider" type="uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentDocumentUrlProvider" -->
<#-- @ftlvariable name="userProcessingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission>" -->

<#macro actions userProcessingPermissions consentDocumentUrlProvider>

    <#assign consentReviewPermission = userProcessingPermissions?seq_contains("CONSENT_REVIEW") />
    <#assign sendForApprovalPermission = userProcessingPermissions?seq_contains("SEND_CONSENT_FOR_APPROVAL") />

    <div class="govuk-button-group">

        <#if consentReviewPermission>
            <@fdsAction.link linkText="Issue consent" linkUrl=springUrl(consentDocumentUrlProvider.issueConsentUrl) linkClass="govuk-button govuk-button--green" />
            <@fdsAction.link linkText="Return to case officer" linkUrl=springUrl(consentDocumentUrlProvider.returnToCaseOfficerUrl) linkClass="govuk-button govuk-button--blue" />
        </#if>

        <#if sendForApprovalPermission>
            <@fdsAction.link linkText="Send for approval" linkUrl=springUrl(consentDocumentUrlProvider.sendForApprovalUrl) linkClass="govuk-button govuk-button--green" />
        </#if>

        <@fdsForm.htmlForm actionUrl=springUrl(consentDocumentUrlProvider.previewUrl)>
            <@fdsAction.button buttonText="Preview document" buttonClass="govuk-button govuk-button--" + consentReviewPermission?then("secondary", "blue") />
        </@fdsForm.htmlForm>

        <#if !consentReviewPermission>
            <@fdsAction.link linkText="Reload document" linkUrl=springUrl(consentDocumentUrlProvider.reloadDocumentUrl) linkClass="govuk-button govuk-button--secondary" />
        </#if>

    </div>

</#macro>