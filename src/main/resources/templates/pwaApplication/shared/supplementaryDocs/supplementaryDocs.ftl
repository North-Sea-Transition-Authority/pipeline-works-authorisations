<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Supplementary documents" pageHeading="Supplementary documents" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.hasFilesToUpload" labelText="Do you want to upload any supplementary documents?" hiddenContent=true>

            <@fdsRadio.radioYes path="form.hasFilesToUpload">
                <@fdsFileUpload.fileUpload
                    id="supplementaryDocumentFiles"
                    path="form.uploadedFiles"
                    uploadUrl=fileUploadAttributes.uploadUrl()
                    downloadUrl=fileUploadAttributes.downloadUrl()
                    deleteUrl=fileUploadAttributes.deleteUrl()
                    maxAllowedSize=fileUploadAttributes.maxAllowedSize()
                    allowedExtensions=fileUploadAttributes.allowedExtensions()
                    existingFiles=fileUploadAttributes.existingFiles()
                    dropzoneText="Drag and drop your documents here"
                />
            </@fdsRadio.radioYes>

            <@fdsRadio.radioNo path="form.hasFilesToUpload"/>

        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>