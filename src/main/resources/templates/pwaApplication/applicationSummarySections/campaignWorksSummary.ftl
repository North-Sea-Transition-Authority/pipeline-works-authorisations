<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="workScheduleViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView>" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="workScheduleDetails">${sectionDisplayText}</h2>

    <@campaignWorksDetails workScheduleViews/>

</div>


<#macro campaignWorksDetails workScheduleViews>

  <#if workScheduleViews?has_content>
    <@fdsCheckAnswers.checkAnswers>
      <#list workScheduleViews as workScheduleView>

        <@fdsCheckAnswers.checkAnswersRow keyText="${workScheduleView.formattedWorkStartDate} - ${workScheduleView.formattedWorkEndDate}" actionUrl="" screenReaderActionText="" actionText="">
          <ul class="govuk-list">
            <#list workScheduleView.schedulePipelines as schedulePipeline>
              <li> ${schedulePipeline.pipelineName} </li>
            </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRow>

      </#list>
    </@fdsCheckAnswers.checkAnswers>

  <#else>
      <@fdsInsetText.insetText>
          No campaign works schedules have been added to this application.
      </@fdsInsetText.insetText>
  </#if>

</#macro>


