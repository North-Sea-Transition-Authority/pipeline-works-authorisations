<#include '../../layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="uploadUrl" type="String" -->
<#-- @ftlvariable name="deleteUrl" type="String" -->
<#-- @ftlvariable name="downloadUrl" type="String" -->
<#-- @ftlvariable name="backButtonText" type="String" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="fileuploadMaxUploadSize" type="String" -->
<#-- @ftlvariable name="fileuploadAllowedExtensions" type="java.util.List<String>" -->
<#-- @ftlvariable name="uploadedFileViewList" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->


<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle breadcrumbs=true>

    <@fdsForm.htmlForm>
        <@fileUpload.fileUpload path="form.uploadedFileWithDescriptionForms" id="doc-upload-file-id" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
        <@fdsAction.submitButtons primaryButtonText="Save and complete" linkSecondaryAction=true secondaryLinkText=backButtonText linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>