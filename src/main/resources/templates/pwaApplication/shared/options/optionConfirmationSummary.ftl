<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionView" -->

<#macro summary view >
  <@fdsCheckAnswers.checkAnswers>
      <#assign multiLineTextBlockClass = "govuk-summary-list" />

      <@fdsCheckAnswers.checkAnswersRow keyText="Confirmed work" actionUrl="" screenReaderActionText="" actionText="">
          ${view.workType!""}
      </@fdsCheckAnswers.checkAnswersRow>

      <#if view.workDescription?has_content>
          <@fdsCheckAnswers.checkAnswersRow keyText="Description of work" actionUrl="" screenReaderActionText="" actionText="">
              <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${view.workDescription!""}</@multiLineText.multiLineText>
          </@fdsCheckAnswers.checkAnswersRow>
      </#if>


  </@fdsCheckAnswers.checkAnswers>

</#macro>