<#include '../../layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="uploadUrl" type="String" -->
<#-- @ftlvariable name="deleteUrl" type="String" -->
<#-- @ftlvariable name="downloadUrl" type="String" -->
<#-- @ftlvariable name="backButtonText" type="String" -->
<#-- @ftlvariable name="backUrl" type="String" -->
<#-- @ftlvariable name="fileuploadMaxUploadSize" type="String" -->
<#-- @ftlvariable name="fileuploadAllowedExtensions" type="java.util.List<String>" -->
<#-- @ftlvariable name="restrictToImageFileTypes" type="Boolean" -->
<#-- @ftlvariable name="singleFileUpload" type="Boolean" -->
<#-- @ftlvariable name="uploadedFileViewList" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->


<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle breadcrumbs=true>

    <#assign singleFileUpload = singleFileUpload?has_content && singleFileUpload == true/>
    <#assign dropzoneText = singleFileUpload?string("Drag and drop your document here", "Drag and drop your documents here")/>

    <@fdsForm.htmlForm>
        <@fdsFileUpload.fileUpload
            id="fileUpload"
            path="form.uploadedFiles"
            uploadUrl=fileUploadAttributes.uploadUrl()
            downloadUrl=fileUploadAttributes.downloadUrl()
            deleteUrl=fileUploadAttributes.deleteUrl()
            maxAllowedSize=fileUploadAttributes.maxAllowedSize()
            allowedExtensions=fileUploadAttributes.allowedExtensions()
            existingFiles=fileUploadAttributes.existingFiles()
            dropzoneText="Drag and drop your documents here"
            multiFile=(singleFileUpload == false)
        />

        <@fdsAction.submitButtons primaryButtonText="Save and complete" linkSecondaryAction=true secondaryLinkText=backButtonText linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>