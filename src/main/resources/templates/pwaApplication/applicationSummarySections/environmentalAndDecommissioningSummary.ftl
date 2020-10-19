<#include '../../pwaLayoutImports.ftl'>
<#include 'appSummaryUtils.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="environmentalDecommView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.EnvironmentalDecommissioningView" -->
<#-- @ftlvariable name="environmentalConditions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition>" -->
<#-- @ftlvariable name="decommissioningConditions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition>" -->
<#-- @ftlvariable name="availableQuestions" type="java.util.Set<uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion>" -->

<div class="pwa-application-summary-section">

  <h2 class="govuk-heading-l" id="environmentalDecommDetails">${sectionDisplayText}</h2>

  <@environmentalDecommDetails environmentalDecommView/>

</div>

<#macro environmentalDecommDetails environmentalDecommView>

  <#local multiLineTextBlockClass = "govuk-summary-list" />

  <@fdsCheckAnswers.checkAnswers>

    <#if availableQuestions?seq_contains("TRANS_BOUNDARY")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Does the development present a significant trans-boundary environmental effect?" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.transboundaryEffect?has_content>
            <@showYesNoForBool environmentalDecommView.transboundaryEffect/>
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if availableQuestions?seq_contains("BEIS_EMT_PERMITS")>

      <@fdsCheckAnswers.checkAnswersRow keyText="Have you submitted any relevant environmental permits to BEIS EMT?" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.emtHasSubmittedPermits?has_content>
            <@showYesNoForBool environmentalDecommView.emtHasSubmittedPermits/>
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>

      <#if environmentalDecommView.emtHasSubmittedPermits?has_content && environmentalDecommView.emtHasSubmittedPermits>
          <@fdsCheckAnswers.checkAnswersRow keyText="Which permits have you submitted to BEIS?" actionUrl="" screenReaderActionText="" actionText="">
              <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${environmentalDecommView.permitsSubmitted!} </@multiLineText.multiLineText>
          </@fdsCheckAnswers.checkAnswersRow>
      </#if>

      <@fdsCheckAnswers.checkAnswersRow keyText="Do you have any environmental permits that have not yet been submitted to BEIS EMT?" actionUrl="" screenReaderActionText="" actionText="">
          <#if environmentalDecommView.emtHasOutstandingPermits?has_content>
              <@showYesNoForBool environmentalDecommView.emtHasOutstandingPermits/>
          </#if>
      </@fdsCheckAnswers.checkAnswersRow>

      <#if environmentalDecommView.emtHasOutstandingPermits?has_content && environmentalDecommView.emtHasOutstandingPermits>
          <@fdsCheckAnswers.checkAnswersRow keyText="Which permits have you not submitted to BEIS?" actionUrl="" screenReaderActionText="" actionText="">
              <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${environmentalDecommView.permitsPendingSubmission!} </@multiLineText.multiLineText>
          </@fdsCheckAnswers.checkAnswersRow>

          <@fdsCheckAnswers.checkAnswersRow keyText="What is the latest date all relevant environmental permits will be submitted to BEIS?" actionUrl="" screenReaderActionText="" actionText="">
              <@showNotProvidedWhenEmpty environmentalDecommView.emtSubmissionDate!/>
          </@fdsCheckAnswers.checkAnswersRow>
      </#if>

    </#if>

  </@fdsCheckAnswers.checkAnswers>

  <#if availableQuestions?seq_contains("ACKNOWLEDGEMENTS")>

    <h3 class="govuk-heading-m"> Acknowledgements </h3>
      <@fdsCheckAnswers.checkAnswers>
          <#list environmentalConditions as environmentalCondition>
              <@fdsCheckAnswers.checkAnswersRow keyText=environmentalCondition.getSummaryText() actionUrl="" screenReaderActionText="" actionText="">
                  <#if environmentalDecommView.environmentalConditions?has_content>
                      ${environmentalDecommView.environmentalConditions?seq_contains(environmentalCondition)?then(environmentalCondition.getConditionText(), 'Not provided')}
                  <#else>
                    Not provided
                  </#if>
              </@fdsCheckAnswers.checkAnswersRow>
          </#list>
      </@fdsCheckAnswers.checkAnswers>

  </#if>

  <#if availableQuestions?seq_contains("DECOMMISSIONING")>

    <h3 class="govuk-heading-m"> Decommissioning </h3>
    <@fdsCheckAnswers.checkAnswers>
      <#list decommissioningConditions as decommissioningCondition>
          <@fdsCheckAnswers.checkAnswersRow keyText=decommissioningCondition.getSummaryText() actionUrl="" screenReaderActionText="" actionText="">
              <#if environmentalDecommView.decommissioningConditions?has_content>
                  ${environmentalDecommView.decommissioningConditions?seq_contains(decommissioningCondition)?then(decommissioningCondition.getConditionText(), 'Not provided')}
              <#else>
                  Not provided
              </#if>
          </@fdsCheckAnswers.checkAnswersRow>
      </#list>
    </@fdsCheckAnswers.checkAnswers>

  </#if>

</#macro>

