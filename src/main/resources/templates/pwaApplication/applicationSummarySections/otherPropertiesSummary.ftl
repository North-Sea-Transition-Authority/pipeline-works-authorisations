<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="otherPropertiesView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.OtherPropertiesView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="otherPropertiesDetails">${sectionDisplayText}</h2>

    <@otherPropertiesDetails otherPropertiesView/>

</div>


<#macro otherPropertiesDetails otherPropertiesView>

 <@fdsCheckAnswers.checkAnswers>

    <#list otherPropertiesView.propertyValueMap as property, propertyValue>
      <@fdsCheckAnswers.checkAnswersRow keyText=property.getDisplayText() actionUrl="" screenReaderActionText="" actionText="">

        <#if propertyValue.propertyAvailabilityOption?has_content>        
          <#if propertyValue.propertyAvailabilityOption == "AVAILABLE">
            <@minMaxSummary.minMaxSummary
              propertyValue.minValue
              propertyValue.maxValue
              'min'
              'max'
              property.getUnitMeasurement().getSuffixDisplay()
            />
          <#else>
            ${propertyValue.propertyAvailabilityOption.getDisplayText()}
          </#if>
        </#if>

      </@fdsCheckAnswers.checkAnswersRow>
    </#list>

    <@fdsCheckAnswers.checkAnswersRow keyText="Phases present" actionUrl="" screenReaderActionText="" actionText="">
      <ul class="govuk-list">
        <#list otherPropertiesView.selectedPropertyPhases?sort as phase>
          <li> ${phase.getDisplayText()} </li>
        </#list>
      </ul>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if otherPropertiesView.selectedPropertyPhases?has_content && otherPropertiesView.selectedPropertyPhases?seq_contains("OTHER")> 
      <@fdsCheckAnswers.checkAnswersRow keyText="Other phase present" actionUrl="" screenReaderActionText="" actionText="">
        ${otherPropertiesView.otherPhaseDescription!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>
      
  </@fdsCheckAnswers.checkAnswers>

</#macro>

