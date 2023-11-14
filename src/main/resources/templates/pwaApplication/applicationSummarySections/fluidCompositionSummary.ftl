<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="fluidCompositionView" type="uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="fluidCompositionDetails">${sectionDisplayText}</h2>

    <@fluidCompositionDetails fluidCompositionView/>

</div>


<#macro fluidCompositionDetails fluidCompositionView>
  <@fdsCheckAnswers.checkAnswers>
    <#list fluidCompositionView.chemicalDataFormMap as chemical, fluidCompositionDataForm>
      <@fdsCheckAnswers.checkAnswersRow keyText=chemical.getDisplayText() actionUrl="" screenReaderActionText="" actionText="">
        <#if fluidCompositionDataForm.chemicalMeasurementType?has_content>
          <#if fluidCompositionDataForm.chemicalMeasurementType == "MOLE_PERCENTAGE">
            ${fluidCompositionDataForm.measurementValue.value}%
          <#elseif fluidCompositionDataForm.chemicalMeasurementType?contains("PPMV")>
              ${fluidCompositionDataForm.measurementValue.value} ppmv
          <#else>
            ${fluidCompositionDataForm.chemicalMeasurementType.getDisplayTextSimple()}
          </#if>
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#list>
  </@fdsCheckAnswers.checkAnswers>
</#macro>


