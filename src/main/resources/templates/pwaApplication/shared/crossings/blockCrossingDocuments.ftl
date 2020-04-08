<#include '../../../layout.ftl'>


<@defaultPage htmlTitle="Block crossing documents" pageHeading="Block crossing documents" breadcrumbs=true>

    <@fdsForm.htmlForm >
        <@fileUpload.fileUpload  path="form.uploadedFileWithDescriptionForms" id="block-crossing-doc-upload-file-id" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
        <@fdsAction.submitButtons primaryButtonText="Save and complete" linkSecondaryAction=true secondaryLinkText="Back to crossings" linkSecondaryActionUrl=springUrl(crossingUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>