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

        <#if fluidCompositionDataForm.fluidCompositionOption?has_content>        
          <#if fluidCompositionDataForm.fluidCompositionOption == "HIGHER_AMOUNT">
            ${fluidCompositionDataForm.moleValue.value}%
          <#else>
            ${fluidCompositionDataForm.fluidCompositionOption.getDisplayTextSimple()}
          </#if>
        </#if>

      </@fdsCheckAnswers.checkAnswersRow>
    </#list>
      
  </@fdsCheckAnswers.checkAnswers>


</#macro>


