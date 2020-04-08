<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Project information" pageHeading="Project information" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.projectName" labelText="Name of project"/>
        <@fdsDateInput.dateInput dayPath="form.proposedStartDay" monthPath="form.proposedStartMonth" yearPath="form.proposedStartYear" labelText="Proposed start date" hintText="What is the date you expect to begin work on your application?" formId="form.proposedStart" defaultHint=false/>
        <@fdsTextarea.textarea path="form.projectOverview" labelText="Overview of project" characterCount=true maxCharacterLength="4000"/>
        <@fdsTextarea.textarea path="form.methodOfPipelineDeployment" labelText="Pipeline installation method" hintText="Brief overview of method that will be deployed for the pipeline installation(s)" characterCount=true maxCharacterLength="4000"/>
        <@fdsDateInput.dateInput dayPath="form.mobilisationDay" monthPath="form.mobilisationMonth" yearPath="form.mobilisationYear" labelText="Date of mobilisation" formId="form.mobilisation"/>
        <@fdsDateInput.dateInput dayPath="form.earliestCompletionDay" monthPath="form.earliestCompletionMonth" yearPath="form.earliestCompletionYear" labelText="Earliest completion date" formId="form.earliestCompletion"/>
        <@fdsDateInput.dateInput dayPath="form.latestCompletionDay" monthPath="form.latestCompletionMonth" yearPath="form.latestCompletionYear" labelText="Latest completion date" formId="form.latestCompletion"/>
        <@fdsRadio.radioGroup path="form.usingCampaignApproach" labelText="Will the work be completed using a campaign approach?">
          <@fdsRadio.radioYes path="form.usingCampaignApproach"/>
          <@fdsRadio.radioNo path="form.usingCampaignApproach"/>
        </@fdsRadio.radioGroup>
        <@fdsFieldset.fieldset legendHeadingClass="govuk-fieldset__legend--l" legendHeading="Project documents" legendHeadingSize="h2" hintText="Provide an overall project layout diagram showing pipeline(s) to be covered by the Authorisation and route of the pipeline(s)." nestingPath="" caption="" captionClass="govuk-caption-l">
            <@fileUpload.fileUpload path="form.uploadedFileWithDescriptionForms" id="project-doc-upload-file-id" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
        </@fdsFieldset.fieldset>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>