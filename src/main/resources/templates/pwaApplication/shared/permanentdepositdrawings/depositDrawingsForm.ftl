<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->

<@defaultPage htmlTitle="${screenAction.actionText} deposit drawing" pageHeading="${screenAction.actionText} deposit drawing" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.reference" labelText="Drawing reference" inputClass="govuk-!-width-two-thirds"/>

        <@fdsFileUpload.fileUpload
            id="depositDrawing"
            path="form.uploadedFiles"
            uploadUrl=fileUploadAttributes.uploadUrl()
            downloadUrl=fileUploadAttributes.downloadUrl()
            deleteUrl=fileUploadAttributes.deleteUrl()
            maxAllowedSize=fileUploadAttributes.maxAllowedSize()
            allowedExtensions=fileUploadAttributes.allowedExtensions()
            existingFiles=fileUploadAttributes.existingFiles()
            dropzoneText="Drag and drop your documents here"
            multiFile=false
        />

        <@fdsSearchSelector.searchSelectorEnhanced path="form.selectedDeposits" options=depositOptions labelText="Select deposits" multiSelect=true />

        <@fdsAction.submitButtons primaryButtonText="${screenAction.submitButtonText} drawing" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>