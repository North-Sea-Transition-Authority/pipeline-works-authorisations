<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="consentDocumentUrlProvider" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentUrlProvider" -->
<#-- @ftlvariable name="userProcessingPermissions" type="java.util.Set<uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission>" -->
<#-- @ftlvariable name="openConsentReview" type="java.lang.Boolean" -->

<#macro actions userProcessingPermissions consentDocumentUrlProvider openConsentReview>

    <#assign consentReviewPermission = userProcessingPermissions?seq_contains("CONSENT_REVIEW") />
    <#assign sendForApprovalPermission = userProcessingPermissions?seq_contains("SEND_CONSENT_FOR_APPROVAL") />
    <#assign consentReviewOpenAndHasPermission = consentReviewPermission && openConsentReview />

    <div class="govuk-button-group">

        <@fdsForm.htmlForm actionUrl=springUrl(consentDocumentUrlProvider.previewUrl)>

            <#if consentReviewOpenAndHasPermission>
                <@fdsAction.link linkText="Issue consent" linkUrl=springUrl(consentDocumentUrlProvider.issueConsentUrl) linkClass="govuk-button govuk-button--green" />
                <@fdsAction.link linkText="Return to case officer" linkUrl=springUrl(consentDocumentUrlProvider.returnToCaseOfficerUrl) linkClass="govuk-button govuk-button--blue" />
            </#if>

            <#if sendForApprovalPermission && !openConsentReview>
                <@fdsAction.link linkText="Send for approval" linkUrl=springUrl(consentDocumentUrlProvider.sendForApprovalUrl) linkClass="govuk-button govuk-button--green" />
            </#if>

            <@fdsAction.button buttonText="Preview document" buttonClass="govuk-button govuk-button--" + consentReviewOpenAndHasPermission?then("secondary", "blue") />

            <#if !consentReviewOpenAndHasPermission>
                <@fdsAction.link linkText="Reload document" linkUrl=springUrl(consentDocumentUrlProvider.reloadDocumentUrl) linkClass="govuk-button govuk-button--secondary" />
            </#if>

        </@fdsForm.htmlForm>

    </div>

</#macro>