<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="fileuploadMaxUploadSize" type="String" -->
<#-- @ftlvariable name="fileuploadAllowedExtensions" type="java.util.List<String>" -->
<#-- @ftlvariable name="uploadedFileViewList" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->
<#-- @ftlvariable name="uploadUrl" type="String" -->
<#-- @ftlvariable name="deleteUrl" type="String" -->
<#-- @ftlvariable name="downloadUrl" type="String" -->
<#-- @ftlvariable name="actionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview>" -->

<@defaultPage htmlTitle="${actionType.actionText} a pipeline schematic" pageHeading="${actionType.actionText} a pipeline schematic" breadcrumbs=true fullWidthColumn=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.reference" labelText="Schematic reference" hintText="This reference must be displayed on the drawing provided" inputClass="govuk-!-width-two-thirds"/>

        <@fdsFileUpload.fileUpload
            id="pipelineDrawing"
            path="form.uploadedFiles"
            uploadUrl=fileUploadAttributes.uploadUrl()
            downloadUrl=fileUploadAttributes.downloadUrl()
            deleteUrl=fileUploadAttributes.deleteUrl()
            maxAllowedSize=fileUploadAttributes.maxAllowedSize()
            allowedExtensions=fileUploadAttributes.allowedExtensions()
            existingFiles=fileUploadAttributes.existingFiles()
            dropzoneText="Drag and drop your documents here"
        />

      <hr class="govuk-section-break govuk-section-break--m"/>

        <@fdsFieldset.fieldset legendHeading="Which pipelines are linked to this schematic?">
            <@pwaPipelineTableSelection.pipelineTableSelection path="form.padPipelineIds" pipelineOverviews=pipelineViews/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText="${actionType.submitButtonText} schematic" linkSecondaryAction=true secondaryLinkText="Back to pipeline schematics and other diagrams" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>