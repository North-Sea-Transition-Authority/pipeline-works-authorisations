<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="summaryView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadBundleSummaryView" -->

<@defaultPage htmlTitle="Remove bundle" pageHeading="Are you sure you want to remove this bundle?" breadcrumbs=true>

    <@fdsCheckAnswers.checkAnswers>
      <@fdsCheckAnswers.checkAnswersRow keyText="Bundle name" actionUrl="" screenReaderActionText="" actionText="">
        ${summaryView.bundleName}
      </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Pipelines in bundle" actionUrl="" screenReaderActionText="" actionText="">
          <ul class="govuk-list">
            <#list summaryView.pipelineReferences as reference>
              <li>${reference}</li>
            </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRow>
    </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
      <@fdsAction.submitButtons primaryButtonText="Remove bundle" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>