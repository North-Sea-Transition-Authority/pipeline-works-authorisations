<#include '../../layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="uploadUrl" type="String" -->
<#-- @ftlvariable name="deleteUrl" type="String" -->
<#-- @ftlvariable name="backButtonText" type="String" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle breadcrumbs=true>

    <@fdsForm.htmlForm>
        <@fileUpload.fileUpload path="form.uploadedFileWithDescriptionForms" id="doc-upload-file-id" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
        <@fdsAction.submitButtons primaryButtonText="Save and complete" linkSecondaryAction=true secondaryLinkText=backButtonText linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>