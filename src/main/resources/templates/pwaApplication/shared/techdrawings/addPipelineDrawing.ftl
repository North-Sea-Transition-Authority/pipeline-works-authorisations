<#include '../../../layout.ftl'>
<#import '../../../components/widgets/pipelineTableSelection.ftl' as pipelineTableSelection/>

<#-- @ftlvariable name="admiraltyChartUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory" -->
<#-- @ftlvariable name="admiraltyChartFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="admiraltyOptional" type="java.lang.Boolean" -->

<@defaultPage htmlTitle="Add a pipeline drawing" pageHeading="Add a pipeline drawing" breadcrumbs=true fullWidthColumn=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.reference" labelText="Drawing reference" inputClass="govuk-!-width-two-thirds"/>
        <@fileUpload.fileUpload id="drawing-file-upload" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl downloadUrl=downloadUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions/>

      <hr class="govuk-section-break govuk-section-break--m"/>

        <@fdsFieldset.fieldset legendHeading="Which pipelines are linked to this drawing?">
            <@pipelineTableSelection.pipelineTableSelection path="form.pipelineIds" pipelineOverviews=pipelineViews/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>