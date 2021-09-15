<#include '../layout.ftl'>

<#-- @ftlvariable name="consentFileView" type="uk.co.ogauthority.pwa.model.view.consent.ConsentFileView" -->

<#macro sosdFileView consentFileView>

    <#if consentFileView.consultationRequestView?has_content>
        <#assign consultationRequestViewData = consentFileView.consultationRequestView />
        <#if consultationRequestViewData.consultationResponseFileViews?has_content>
            <h2 class="govuk-heading-m">Secretary of State decision files</h2>
            <@pwaFiles.uploadedFileList downloadUrl=springUrl(consultationRequestViewData.downloadFileUrl) existingFiles=consultationRequestViewData.consultationResponseFileViews />
        </#if>
    </#if>

</#macro>

