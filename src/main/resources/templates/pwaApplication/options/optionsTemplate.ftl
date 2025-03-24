<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="ogaOptionsTemplateLink" type="java.lang.String" -->

<@defaultPage htmlTitle="Options template" pageHeading="Options template" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <#assign optionsTemplateLink>
            <@fdsAction.link
            linkText="the NSTA website"
            linkUrl=ogaOptionsTemplateLink
            openInNewTab=true
            linkClass="govuk-link"
            linkScreenReaderText="NSTA options template link"/>
        </#assign>

        <@fdsFieldset.fieldset legendHeading="Template document" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2" hintText="Upload a completed template from ${optionsTemplateLink}">
            <@fdsFileUpload.fileUpload
                id="templateDocument"
                path="form.uploadedFiles"
                uploadUrl=fileUploadAttributes.uploadUrl()
                downloadUrl=fileUploadAttributes.downloadUrl()
                deleteUrl=fileUploadAttributes.deleteUrl()
                maxAllowedSize=fileUploadAttributes.maxAllowedSize()
                allowedExtensions=fileUploadAttributes.allowedExtensions()
                existingFiles=fileUploadAttributes.existingFiles()
                dropzoneText="Drag and drop your documents here"
            />
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>