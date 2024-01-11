<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<#-- @ftlvariable name="showAreaNames" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hideAreaNamesOnLoad" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showPwaLinkedToDesc" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hidePwaLinkedToDescOnLoad" type="java.lang.Boolean" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="areaInformation">${sectionDisplayText}</h2>

    <@fdsCheckAnswers.checkAnswers>

      <@fdsCheckAnswers.checkAnswersRow keyText="Are any storage sites covered by this PWA?" actionUrl="" screenReaderActionText="" actionText="">
        <@diffChanges.renderDiff areaLinkQuestions.PwaAreaLinksView_isLinkedToAreas />
      </@fdsCheckAnswers.checkAnswersRow>

      <#assign diffHideGroup = "hide-when-diff-disabled"/>

      <#if showPwaLinkedToDesc>
        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="What is this PWA related to?" rowClass=hidePwaLinkedToDescOnLoad?then(diffHideGroup, "")>
          <@diffChanges.renderDiff diffedField=areaLinkQuestions.PwaAreaLinksView_pwaLinkedToDescription multiLineTextBlockClass="govuk-summary-list"/>
        </@fdsCheckAnswers.checkAnswersRowNoAction>
      </#if>

      <#if showAreaNames>
        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Linked storage sites" rowClass=hideAreaNamesOnLoad?then(diffHideGroup, "")>
          <ul class="govuk-list">
          <#list areaLinks as area>

            <li><@diffChanges.renderDiff area.StringWithTagItem_stringWithTag /></li>

          </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRowNoAction>
      </#if>

    </@fdsCheckAnswers.checkAnswers>


</div>
