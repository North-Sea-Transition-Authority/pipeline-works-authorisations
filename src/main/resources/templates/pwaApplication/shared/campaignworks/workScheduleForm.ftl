<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelineViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview>" -->

<@defaultPage htmlTitle="Add work schedule" pageHeading="Add work schedule" breadcrumbs=true>

   <@fdsForm.htmlForm>
       <@fdsNumberInput.twoNumberInputs pathOne="form.workStart.month" pathTwo="form.workStart.year" labelText="Expected start of work" formId="start-month-year">
           <@fdsNumberInput.numberInputItem path="form.workStart.month" labelText="Month" inputClass="govuk-input--width-2"/>
           <@fdsNumberInput.numberInputItem path="form.workStart.year" labelText="Year" inputClass="govuk-input--width-4"/>
       </@fdsNumberInput.twoNumberInputs>

       <@fdsNumberInput.twoNumberInputs pathOne="form.workEnd.month" pathTwo="form.workEnd.year" labelText="Expected end of work" formId="end-month-year">
           <@fdsNumberInput.numberInputItem path="form.workEnd.month" labelText="Month" inputClass="govuk-input--width-2"/>
           <@fdsNumberInput.numberInputItem path="form.workEnd.year" labelText="Year" inputClass="govuk-input--width-4"/>
       </@fdsNumberInput.twoNumberInputs>
       
       <@fdsFieldset.fieldset legendHeading="Which pipelines are included in this work?" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2">
           <@pwaPipelineTableSelection.pipelineTableSelection path="form.padPipelineIds" pipelineOverviews=pipelineViews/>
       </@fdsFieldset.fieldset>

       <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add work schedule" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
   </@fdsForm.htmlForm>

</@defaultPage>