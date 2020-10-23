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
        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="What is this PWA in relation to?" rowClass=hidePwaLinkedToDescOnLoad?then(diffHideGroup, "")>
          <@diffChanges.renderDiff diffedField=fieldLinkQuestions.PwaFieldLinksView_pwaLinkedToDescription multiLineTextBlockClass="govuk-summary-list"/>
        </@fdsCheckAnswers.checkAnswersRowNoAction>
      </#if>

      <#if showFieldNames>
        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Linked fields" rowClass=hideFieldNamesOnLoad?then(diffHideGroup, "")>
          <ul class="govuk-list">
          <#list fieldLinks as field>

            <li><@diffChanges.renderDiff field.StringWithTagItem_stringWithTag /></li>

          </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRowNoAction>
      </#if>

    </@fdsCheckAnswers.checkAnswers>


</div>
