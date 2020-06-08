<#include '../../../layout.ftl'>

<#-- @ftlvariable name="admiraltyOptional" type="java.lang.Boolean" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="fileuploadMaxUploadSize" type="String" -->
<#-- @ftlvariable name="fileuploadAllowedExtensions" type="java.util.List<String>" -->
<#-- @ftlvariable name="uploadedFileViewList" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="uploadUrl" type="String" -->
<#-- @ftlvariable name="deleteUrl" type="String" -->
<#-- @ftlvariable name="downloadUrl" type="String" -->
<#-- @ftlvariable name="actionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview>" -->

<@defaultPage htmlTitle="${actionType.actionText} a pipeline drawing" pageHeading="${actionType.actionText} a pipeline drawing" breadcrumbs=true fullWidthColumn=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.reference" labelText="Drawing reference" inputClass="govuk-!-width-two-thirds"/>
        <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>

      <hr class="govuk-section-break govuk-section-break--m"/>

        <@fdsFieldset.fieldset legendHeading="Which pipelines are linked to this drawing?">
            <@pwaPipelineTableSelection.pipelineTableSelection path="form.padPipelineIds" pipelineOverviews=pipelineViews/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText="${actionType.submitButtonText} drawing" linkSecondaryAction=true secondaryLinkText="Back to admiralty chart and pipeline drawings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>