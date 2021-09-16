<#include '../layout.ftl'>

<#-- @ftlvariable name="consultationRequestView" type="java.util.List<"uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView>" -->

<#macro sosdFileView consultationRequestView=[]>

    <#if consultationRequestView?has_content>
        <#if consultationRequestView.consultationResponseFileViews?has_content>
            <h2 class="govuk-heading-m">Secretary of State decision files</h2>
            <@pwaFiles.uploadedFileList downloadUrl=springUrl(consultationRequestView.downloadFileUrl) existingFiles=consultationRequestView.consultationResponseFileViews />
        </#if>
    </#if>

</#macro>

