<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Project information" pageHeading="Project information" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.projectName" labelText="Name of project"/>
        <@fdsDateInput.dateInput dayPath="form.proposedStartDay" monthPath="form.proposedStartMonth" yearPath="form.proposedStartYear" labelText="Proposed start date" hintText="What is the date you expect to begin work on your application?" formId="form.proposedStart"/>
        <@fdsTextarea.textarea path="form.projectOverview" labelText="Overview of project" characterCount=true maxCharacterLength="4000"/>
        <@fdsTextarea.textarea path="form.methodOfPipelineDeployment" labelText="Pipeline installation method" hintText="Brief overview of method that will be deployed for the pipeline installation(s)?" characterCount=true maxCharacterLength="4000"/>
        <!-- TODO: !!PWA-370!! Add file upload widget -->
        <@fdsAction.submitButtons primaryButtonText="Submit"/>
    </@fdsForm.htmlForm>

</@defaultPage>