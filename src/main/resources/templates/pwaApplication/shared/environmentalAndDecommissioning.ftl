<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="hseSafetyZones" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="environmentalConditions" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="decommissioningConditions" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="availableQuestions" type="java.util.Set<uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion>" -->

<@defaultPage htmlTitle="Environmental and decommissioning" pageHeading="Environmental and decommissioning" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

      <h2 class="govuk-heading-l">Environmental</h2>

        <#if availableQuestions?seq_contains("TRANS_BOUNDARY")>

          <@fdsDetails.details detailsTitle="What is the ESPOO convention?"
                  detailsText="The ESPOO (EIA) Convention sets out the obligations of Parties to assess the environmental impact of certain activities at an early stage of planning. It also lays down the general obligation of States to notify and consult each other on all major projects under consideration that are likely to have a significant adverse environmental impact across boundaries."/>
          <@fdsRadio.radioGroup path="form.transboundaryEffect" labelText="Does the development present a significant trans-boundary environmental effect?" hintText="As described in the ESPOO Convention" fieldsetHeadingSize="h3">
              <@fdsRadio.radioYes path="form.transboundaryEffect"/>
              <@fdsRadio.radioNo path="form.transboundaryEffect"/>
          </@fdsRadio.radioGroup>

        </#if>

        <#if availableQuestions?seq_contains("BEIS_EMT_PERMITS")>

          <@fdsFieldset.fieldset legendHeading="BEIS EMT" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">

              <@fdsRadio.radioGroup path="form.emtHasSubmittedPermits" labelText="Have you submitted any relevant environmental permits to BEIS EMT?" fieldsetHeadingSize="h4" hiddenContent=true>
                  <@fdsRadio.radioYes path="form.emtHasSubmittedPermits">
                      <@fdsTextarea.textarea path="form.permitsSubmitted" nestingPath="form.permitsSubmitted" labelText="Which permits have you submitted to BEIS?" hintText="Include the date submitted for each permit" characterCount=true maxCharacterLength="4000"/>
                  </@fdsRadio.radioYes>
                  <@fdsRadio.radioNo path="form.emtHasSubmittedPermits"/>
              </@fdsRadio.radioGroup>

              <@fdsRadio.radioGroup path="form.emtHasOutstandingPermits" labelText="Do you have any environmental permits that have not yet been submitted to BEIS EMT?" fieldsetHeadingSize="h4" hiddenContent=true>
                  <@fdsRadio.radioYes path="form.emtHasOutstandingPermits">
                      <@fdsTextarea.textarea path="form.permitsPendingSubmission" labelText="Which permits have you not submitted to BEIS?" nestingPath="form.permitsPendingSubmission" characterCount=true maxCharacterLength="4000"/>
                      <@fdsDateInput.dateInput formId="emtSubmissionDay" dayPath="form.emtSubmissionDay" monthPath="form.emtSubmissionMonth" yearPath="form.emtSubmissionYear" labelText="What is the latest date all relevant environmental permits will be submitted to BEIS?" hintText="BEIS will require these permits prior to their assessment of your PWA" fieldsetHeadingSize="h5" defaultHint=false/>
                  </@fdsRadio.radioYes>
                  <@fdsRadio.radioNo path="form.emtHasOutstandingPermits"/>
              </@fdsRadio.radioGroup>

          </@fdsFieldset.fieldset>

        </#if>

        <#if availableQuestions?seq_contains("ACKNOWLEDGEMENTS")>
          <@fdsFieldset.fieldset legendHeading="Acknowledgements" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
              <@fdsCheckbox.checkboxes path="form.environmentalConditions" checkboxes=environmentalConditions />
          </@fdsFieldset.fieldset>
        </#if>

        <#if availableQuestions?seq_contains("DECOMMISSIONING")>
          <@fdsFieldset.fieldset legendHeading="Decommissioning" legendHeadingSize="h2">
              <@fdsCheckbox.checkboxes path="form.decommissioningConditions" checkboxes=decommissioningConditions />
          </@fdsFieldset.fieldset>
        </#if>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>
    </@fdsForm.htmlForm>

</@defaultPage>