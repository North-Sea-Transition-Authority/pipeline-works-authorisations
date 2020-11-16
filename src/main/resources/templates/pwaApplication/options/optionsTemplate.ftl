<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="ogaOptionsTemplateLink" type="java.lang.String" -->

<@defaultPage htmlTitle="Options template" pageHeading="Options template" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <#assign optionsTemplateLink>
            <@fdsAction.link
            linkText="the OGA website"
            linkUrl=ogaOptionsTemplateLink
            linkClass="govuk-link"
            linkScreenReaderText="OGA options template link"/>
        </#assign>

        <@fdsFieldset.fieldset legendHeading="Template document" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2" hintText="Upload a completed template from ${optionsTemplateLink}">
            <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>