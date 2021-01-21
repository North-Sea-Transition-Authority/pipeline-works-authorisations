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
<#-- @ftlvariable name="uploadedFileViewList" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->


<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle breadcrumbs=true>

    <#assign singleFileUpload = singleFileUpload?has_content && singleFileUpload == true/>
    <#assign dropzoneText = singleFileUpload?string("Drag and drop your document here", "Drag and drop your documents here")/>

    <@fdsForm.htmlForm>

        <#assign allowedExtensions = fileuploadAllowedExtensions/>
        <#if restrictToImageFileTypes?has_content && restrictToImageFileTypes>
            <#assign allowedExtensions = imageFileUploadAllowedExtensions/>
        </#if>

        <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=allowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText=dropzoneText multiFile=(singleFileUpload == false)/>
        <@fdsAction.submitButtons primaryButtonText="Save and complete" linkSecondaryAction=true secondaryLinkText=backButtonText linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>