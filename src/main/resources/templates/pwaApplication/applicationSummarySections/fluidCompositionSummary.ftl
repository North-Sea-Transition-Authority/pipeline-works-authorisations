<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="fluidCompositionView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.FluidCompositionView" -->


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
            ${fluidCompositionDataForm.moleValue}%
          <#elseif fluidCompositionDataForm.fluidCompositionOption == "TRACE">
             ${fluidCompositionDataForm.fluidCompositionOption.getDisplayText()?keep_before('(')}
          <#else>
            ${fluidCompositionDataForm.fluidCompositionOption.getDisplayText()}
          </#if>
        </#if>

      </@fdsCheckAnswers.checkAnswersRow>
    </#list>
      
  </@fdsCheckAnswers.checkAnswers>


</#macro>


