<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="designOpConditionsView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.DesignOpConditionsView" -->
<#-- @ftlvariable name="unitMeasurements" type="uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="designOpConditionsDetails">${sectionDisplayText}</h2>

    <@designOpConditionsDetails designOpConditionsView/>

</div>


<#macro designOpConditionsDetails designOpConditionsView>  

  <@fdsCheckAnswers.checkAnswers>

    <@fdsCheckAnswers.checkAnswersRow keyText="Temperature operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView minValue=designOpConditionsView.temperatureOpPairValue.valueOne! maxValue=designOpConditionsView.temperatureOpPairValue.valueTwo! unitMeasurement=unitMeasurements.DEGREES_CELSIUS/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Temperature design conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView minValue=designOpConditionsView.temperatureDesignPairValue.valueOne! maxValue=designOpConditionsView.temperatureDesignPairValue.valueTwo! unitMeasurement=unitMeasurements.DEGREES_CELSIUS/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Pressure operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView minValue=designOpConditionsView.pressureOpPairValue.valueOne! maxValue=designOpConditionsView.pressureOpPairValue.valueTwo! 
       unitMeasurement=unitMeasurements.BAR_G altMinLabel="internal" altMaxLabel="external"/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Pressure design conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView minValue=designOpConditionsView.pressureDesignPairValue.valueOne! maxValue=designOpConditionsView.pressureDesignPairValue.valueTwo! 
       unitMeasurement=unitMeasurements.BAR_G altMinLabel="internal" altMaxLabel="external"/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Flowrate operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView minValue=designOpConditionsView.flowrateOpPairValue.valueOne! maxValue=designOpConditionsView.flowrateOpPairValue.valueTwo! unitMeasurement=unitMeasurements.KSCM_D/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Flowrate design conditions" actionUrl="" screenReaderActionText="" actionText="">
       <@minMaxView minValue=designOpConditionsView.flowrateDesignPairValue.valueOne! maxValue=designOpConditionsView.flowrateDesignPairValue.valueTwo! unitMeasurement=unitMeasurements.KSCM_D/>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="U-value operating conditions" actionUrl="" screenReaderActionText="" actionText="">
       <#if designOpConditionsView.uvalueOp?has_content> ${designOpConditionsView.uvalueOp} ${unitMeasurements.KSCM_D.suffixDisplay} </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="U-value design conditions" actionUrl="" screenReaderActionText="" actionText="">
        <#if designOpConditionsView.uvalueDesign?has_content> ${designOpConditionsView.uvalueDesign!} ${unitMeasurements.KSCM_D.suffixDisplay} </#if>
    </@fdsCheckAnswers.checkAnswersRow>
    
      
  </@fdsCheckAnswers.checkAnswers>

</#macro>



<#macro minMaxView  minValue maxValue unitMeasurement altMinLabel="min" altMaxLabel="max">
  <#if minValue?has_content> 
    ${altMinLabel}: ${minValue} ${stringUtils.superscriptConverter(unitMeasurement.suffixDisplay)} </br> 
  </#if>
  <#if maxValue?has_content> 
    ${altMaxLabel}: ${maxValue} ${stringUtils.superscriptConverter(unitMeasurement.suffixDisplay)} 
  </#if>
</#macro>