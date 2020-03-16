<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Environmental and decommissioning" pageHeading="Environmental and decommissioning" breadcrumbs=false>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
      <h2 class="govuk-heading-l">Environmental</h2>
        <@fdsRadio.radioGroup path="form.transboundaryEffect" labelText="Does the development present a significant trans-boundary environmental effect?" hintText="As described in the ESPOO Convention" fieldsetHeadingClass="govuk-fieldset__legend--m" fieldsetHeadingSize="h3">
            <@fdsRadio.radioYes path="form.transboundaryEffect"/>
            <@fdsRadio.radioNo path="form.transboundaryEffect"/>
        </@fdsRadio.radioGroup>

        <@fdsFieldset.fieldset legendHeading="BEIS EMT" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">

            <@fdsRadio.radioGroup path="form.emtHasSubmittedPermits" labelText="Have you submitted any relevant environmental permits to BEIS EMT?" fieldsetHeadingSize="h4" fieldsetHeadingClass="govuk-fieldset__legend--s" hiddenContent=true>
                <@fdsRadio.radioYes path="form.emtHasSubmittedPermits">
                    <@fdsTextarea.textarea path="form.permitsSubmitted" nestingPath="form.permitsSubmitted" labelText="Which permits have you submitted BEIS?" hintText="You must include the date submitted for each permit" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.emtHasSubmittedPermits"/>
            </@fdsRadio.radioGroup>

            <@fdsRadio.radioGroup path="form.emtHasOutstandingPermits" labelText="Do you have any environmental permits that have not yet been submitted to BEIS EMT?" fieldsetHeadingSize="h4" fieldsetHeadingClass="govuk-fieldset__legend--s" hiddenContent=true>
                <@fdsRadio.radioYes path="form.emtHasOutstandingPermits">
                    <@fdsTextarea.textarea path="form.permitsPendingSubmission" labelText="Which permits have you not submitted to BEIS?" nestingPath="form.permitsPendingSubmission" characterCount=true maxCharacterLength="4000"/>
                    <@fdsDateInput.dateInput formId="emtSubmissionDay" dayPath="form.emtSubmissionDay" monthPath="form.emtSubmissionMonth" yearPath="form.emtSubmissionYear" labelText="What is the latest date all relevant environmental permits will be submitted to BEIS?" hintText="BEIS will require these permits prior to their assessment of your PWA" fieldsetHeadingSize="h5" fieldsetHeadingClass="govuk-fieldset__legend--s"/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.emtHasOutstandingPermits"/>
            </@fdsRadio.radioGroup>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Acknowledgements" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
            <@fdsCheckbox.checkbox path="form.dischargeFundsAvailable" labelText="I hereby confirm that the holder has funds available to discharge any liability for damage attributable to the release or escape of anything from the pipeline."/>
            <@fdsCheckbox.checkbox path="form.acceptsOpolLiability" labelText="I acknowledge liability insurance in respect of North Sea operations is arranged under the General Third Party Liability Risk Insurance and the complementary arrangements effected under the Offshore Pollution Liability Agreement (OPOL) of the holder."/>
        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Decommissioning" legendHeadingSize="h2">
            <@fdsTextarea.textarea path="form.decommissioningPlans" labelText="What are your decommissioning plans?"/>
            <@fdsCheckbox.checkbox path="form.acceptsEolRegulations" labelText="I accept that options for the decommissioning of the pipeline(s) will be considered at the end of the field life and should adhere to government policies and regulations in force at the time."/>
            <@fdsCheckbox.checkbox path="form.acceptsEolRemoval" labelText="I accept that any mattresses or grout bags which have been installed to protect pipelines during their operational life should be removed for disposal onshore."/>
            <@fdsCheckbox.checkbox path="form.acceptsRemovalProposal" labelText="I accept that if the condition of the mattresses or grout bags is such that they cannot be removed safely or efficiently then any proposal to leave them in place must be supported by an appropriate comparative assessment of the options."/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>