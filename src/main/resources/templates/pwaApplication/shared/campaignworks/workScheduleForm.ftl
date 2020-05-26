<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview>" -->

<@defaultPage htmlTitle="Add work schedule" pageHeading="Add work schedule" breadcrumbs=true>

   <@fdsForm.htmlForm>
       <@fdsDateInput.dateInput dayPath="form.workStartDay" monthPath="form.workStartMonth" yearPath="form.workStartYear" labelText="Work start date" formId="form.workStart" defaultHint=true/>
       <@fdsDateInput.dateInput dayPath="form.workEndDay" monthPath="form.workEndMonth" yearPath="form.workEndYear" labelText="Work end date" formId="form.workEnd" defaultHint=false/>

       <@fdsFieldset.fieldset legendHeading="Which pipelines are included in this work?" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2">
           <@pwaPipelineTableSelection.pipelineTableSelection path="form.padPipelineIds" pipelineOverviews=pipelineViews/>
       </@fdsFieldset.fieldset>

       <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add work schedule" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
   </@fdsForm.htmlForm>

</@defaultPage>