<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="designOpConditionsView" type="uk.co.ogauthority.pwa.features.application.tasks.designopconditions.DesignOpConditionsView" -->
<#-- @ftlvariable name="unitMeasurements" type="uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="designOpConditionsDetails">${sectionDisplayText}</h2>

    <@designOpConditionsDetails designOpConditionsView/>

</div>


<#macro designOpConditionsDetails designOpConditionsView>  

  <@fdsCheckAnswers.checkAnswers>

    <@fdsCheckAnswers.checkAnswersRow keyText="Temperature operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView designOpConditionsView.temperatureOpMinMaxView/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Temperature design conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView designOpConditionsView.temperatureDesignMinMaxView/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Pressure operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView designOpConditionsView.pressureOpMinMaxView/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Pressure design conditions" actionUrl="" screenReaderActionText="" actionText="">
       <#if designOpConditionsView.pressureDesignMax?has_content> ${designOpConditionsView.pressureDesignMax!} ${unitMeasurements.BAR_G.suffixDisplay} </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Flowrate operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView designOpConditionsView.flowrateOpMinMaxView/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Flowrate design conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView designOpConditionsView.flowrateDesignMinMaxView/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="U-value design conditions" actionUrl="" screenReaderActionText="" actionText="">
        <#if designOpConditionsView.uvalueDesign?has_content> ${designOpConditionsView.uvalueDesign!} ${unitMeasurements.KSCM_D.suffixDisplay} </#if>
    </@fdsCheckAnswers.checkAnswersRow>
    
      
  </@fdsCheckAnswers.checkAnswers>

</#macro>

<#macro minMaxView minMaxViewData>
  <@minMaxSummary.minMaxSummary
    minMaxViewData.minValue
    minMaxViewData.maxValue
    minMaxViewData.minPrompt
    minMaxViewData.maxPrompt
    stringUtils.superscriptConverter(minMaxViewData.unitMeasurement.suffixDisplay)
  />
</#macro>