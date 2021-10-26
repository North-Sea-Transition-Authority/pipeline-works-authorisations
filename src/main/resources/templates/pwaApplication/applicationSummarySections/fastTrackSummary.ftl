<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="fastTrackView" type="uk.co.ogauthority.pwa.features.application.tasks.fasttrack.FastTrackView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="fastTrackDetails">${sectionDisplayText}</h2>
    
    <#if fastTrackView.fastTrackDataExists>    
      <h3 class="govuk-heading-m"> Reasons for fast-tracking your application </h3>
      <@fastTrackDetails fastTrackView/>
    <#else>
      <@fdsInsetText.insetText>No reasons have been given for fast tracking this application</@fdsInsetText.insetText>
    </#if>

    

</div>


<#macro fastTrackDetails fastTrackView>  

  <#local multiLineTextBlockClass = "govuk-summary-list" />

  <@fdsCheckAnswers.checkAnswers>

    <#if fastTrackView.avoidEnvironmentalDisaster?has_content && fastTrackView.avoidEnvironmentalDisaster>
      <@fdsCheckAnswers.checkAnswersRow keyText="Avoiding environmental disaster" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${fastTrackView.environmentalDisasterReason!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if fastTrackView.savingBarrels?has_content && fastTrackView.savingBarrels>
      <@fdsCheckAnswers.checkAnswersRow keyText="Save barrels" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${fastTrackView.savingBarrelsReason!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if fastTrackView.projectPlanning?has_content && fastTrackView.projectPlanning>
      <@fdsCheckAnswers.checkAnswersRow keyText="Project planning" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${fastTrackView.projectPlanningReason!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if fastTrackView.hasOtherReason?has_content && fastTrackView.hasOtherReason>
      <@fdsCheckAnswers.checkAnswersRow keyText="Other reason" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${fastTrackView.otherReason!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    
      
  </@fdsCheckAnswers.checkAnswers>

</#macro>



