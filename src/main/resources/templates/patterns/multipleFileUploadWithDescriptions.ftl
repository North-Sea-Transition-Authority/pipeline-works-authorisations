<#include '../layout.ftl'>

<@defaultPage htmlTitle="Upload file" pageHeading="Upload file">
  <@fdsError.errorSummary errorItems=errorList/>
  <@fdsForm.htmlForm >
    <h2 class="govuk-visually-hidden">Upload file</h2>

    <@fileUpload.fileUpload id="upload-file-id" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
    <@fdsAction.submitButtons primaryButtonText="upload file" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl="${springUrl(cancelUrl)}" />
  </@fdsForm.htmlForm>
</@defaultPage>