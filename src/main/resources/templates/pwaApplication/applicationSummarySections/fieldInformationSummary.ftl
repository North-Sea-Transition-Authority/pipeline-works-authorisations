<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<#-- @ftlvariable name="showFieldNames" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hideFieldNamesOnLoad" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showPwaLinkedToDesc" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hidePwaLinkedToDescOnLoad" type="java.lang.Boolean" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="fieldInformation">${sectionDisplayText}</h2>

    <@fdsCheckAnswers.checkAnswers>

      <@fdsCheckAnswers.checkAnswersRow keyText="PWA is linked to field" actionUrl="" screenReaderActionText="" actionText="">
        <@diffChanges.renderDiff fieldLinkQuestions.PwaFieldLinksView_isLinkedToFields />
      </@fdsCheckAnswers.checkAnswersRow>

      <#assign diffHideGroup = "hide-when-diff-disabled"/>

      <#if showPwaLinkedToDesc>
        <@pwaHideableCheckAnswersRow.hideableCheckAnswersRow keyText="What is this PWA in relation to?"
          actionUrl=""
          screenReaderActionText=""
          actionText=""
          rowClass=hidePwaLinkedToDescOnLoad?then(diffHideGroup, "")>
          <@diffChanges.renderDiff diffedField=fieldLinkQuestions.PwaFieldLinksView_pwaLinkedToDescription multiLineTextBlockClass="govuk-summary-list"/>
        </@pwaHideableCheckAnswersRow.hideableCheckAnswersRow>
      </#if>

      <#if showFieldNames>
        <@pwaHideableCheckAnswersRow.hideableCheckAnswersRow
          keyText="Linked fields"
          actionUrl=""
          screenReaderActionText=""
          actionText=""
          rowClass=hideFieldNamesOnLoad?then(diffHideGroup, "")>
          <ul class="govuk-list">
          <#list fieldLinks as field>

            <li><@diffChanges.renderDiff field.StringWithTagItem_stringWithTag /></li>

          </#list>
          </ul>
        </@pwaHideableCheckAnswersRow.hideableCheckAnswersRow>
      </#if>

    </@fdsCheckAnswers.checkAnswers>


</div>
