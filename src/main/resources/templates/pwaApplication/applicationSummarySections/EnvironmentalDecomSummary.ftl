<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="environmentalDecommView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.EnvironmentalDecommissioningView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="environmentalDecommDetails">${sectionDisplayText}</h2>

    <@environmentalDecommDetails environmentalDecommView/>

</div>


<#macro environmentalDecommDetails environmentalDecommView>

  <#local multiLineTextBlockClass = "govuk-summary-list" />

  <@fdsCheckAnswers.checkAnswers>

    <@fdsCheckAnswers.checkAnswersRow keyText="Does the development present a significant trans-boundary environmental effect?" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.transboundaryEffect?has_content>
            ${environmentalDecommView.transboundaryEffect?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Have you submitted any relevant environmental permits to BEIS EMT?" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.emtHasSubmittedPermits?has_content>
            ${environmentalDecommView.emtHasSubmittedPermits?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if environmentalDecommView.emtHasSubmittedPermits?has_content && environmentalDecommView.emtHasSubmittedPermits>
        <@fdsCheckAnswers.checkAnswersRow keyText="Which permits have you submitted to BEIS?" actionUrl="" screenReaderActionText="" actionText="">
            <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${environmentalDecommView.permitsSubmitted!} </@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <@fdsCheckAnswers.checkAnswersRow keyText="Do you have any environmental permits that have not yet been submitted to BEIS EMT?" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.emtHasOutstandingPermits?has_content>
            ${environmentalDecommView.emtHasOutstandingPermits?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if environmentalDecommView.emtHasOutstandingPermits?has_content && environmentalDecommView.emtHasOutstandingPermits>
        <@fdsCheckAnswers.checkAnswersRow keyText="Which permits have you not submitted to BEIS?" actionUrl="" screenReaderActionText="" actionText="">
            <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${environmentalDecommView.permitsPendingSubmission!} </@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if environmentalDecommView.emtHasOutstandingPermits?has_content && environmentalDecommView.emtHasOutstandingPermits>
        <@fdsCheckAnswers.checkAnswersRow keyText="What is the latest date all relevant environmental permits will be submitted to BEIS?" actionUrl="" screenReaderActionText="" actionText="">
            ${environmentalDecommView.emtSubmissionDate!}
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>
      
  </@fdsCheckAnswers.checkAnswers>



  <h3 class="govuk-heading-m"> Acknowledgements </h3>
  <@fdsCheckAnswers.checkAnswers>
    <@fdsCheckAnswers.checkAnswersRow keyText="The holder has funds available to discharge any liability for damage attributable to the release or escape of anything from the pipeline" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.environmentalConditions?has_content>
            ${environmentalDecommView.environmentalConditions?seq_contains("DISCHARGE_FUNDS_AVAILABLE")?then('Confirmed', 'Unconfirmed')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Liability insurance in respect of North Sea operations is arranged under the General Third Party Liability Risk Insurance and the complementary arrangements effected under the Offshore Pollution Liability Agreement (OPOL) of the holder" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.environmentalConditions?has_content>
            ${environmentalDecommView.environmentalConditions?seq_contains("OPOL_LIABILITY_STATEMENT")?then('Acknowledged', 'Rejected')}
        </#if>        
    </@fdsCheckAnswers.checkAnswersRow>      
  </@fdsCheckAnswers.checkAnswers>



  <h3 class="govuk-heading-m"> Decommissioning </h3>
  <@fdsCheckAnswers.checkAnswers>
    <@fdsCheckAnswers.checkAnswersRow keyText="I accept that options for the decommissioning of the pipeline(s) will be considered at the end of the field life and should adhere to government policies and regulations in force at the time" actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.decommissioningConditions?has_content>
            ${environmentalDecommView.decommissioningConditions?seq_contains("EOL_REGULATION_STATEMENT")?then('Accepted', 'Rejected')}
        </#if>        
    </@fdsCheckAnswers.checkAnswersRow>      

    <@fdsCheckAnswers.checkAnswersRow keyText="I accept that any mattresses or grout bags which have been installed to protect pipelines during their operational life should be removed for disposal onshore." actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.decommissioningConditions?has_content>
            ${environmentalDecommView.decommissioningConditions?seq_contains("EOL_REMOVAL_STATEMENT")?then('Accepted', 'Rejected')}
        </#if>        
    </@fdsCheckAnswers.checkAnswersRow>     

    <@fdsCheckAnswers.checkAnswersRow keyText="I accept that if the condition of the mattresses or grout bags is such that they cannot be removed safely or efficiently then any proposal to leave them in place must be supported by an appropriate comparative assessment of the options." actionUrl="" screenReaderActionText="" actionText="">
        <#if environmentalDecommView.decommissioningConditions?has_content>
            ${environmentalDecommView.decommissioningConditions?seq_contains("EOL_REMOVAL_PROPOSAL")?then('Accepted', 'Rejected')}
        </#if>        
    </@fdsCheckAnswers.checkAnswersRow>     
  </@fdsCheckAnswers.checkAnswers>


</#macro>

