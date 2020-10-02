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
        <@hideableCheckAnswersRow keyText="What is this PWA in relation to?"
          actionUrl=""
          screenReaderActionText=""
          actionText=""
          rowClass=hidePwaLinkedToDescOnLoad?then(diffHideGroup, "")>
          <@diffChanges.renderDiff diffedField=fieldLinkQuestions.PwaFieldLinksView_pwaLinkedToDescription multiLineTextBlockClass="govuk-summary-list"/>
        </@hideableCheckAnswersRow>
      </#if>

      <#if showFieldNames>
        <@hideableCheckAnswersRow
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
        </@hideableCheckAnswersRow>
      </#if>

    </@fdsCheckAnswers.checkAnswers>


</div>

<#-- This is a workaround as FDS does not allow classes to be given to the check answers row -->
<#macro hideableCheckAnswersRow keyText actionUrl screenReaderActionText actionText="Change" rowClass="">
  <div class="govuk-summary-list__row ${rowClass}">
    <dt class="govuk-summary-list__key">
        ${keyText}
    </dt>
    <dd class="govuk-summary-list__value">
        <#nested>
    </dd>
      <#if actionText?has_content>
        <dd class="govuk-summary-list__actions">
          <a class="govuk-link" href="${actionUrl}">
              ${actionText}<span class="govuk-visually-hidden"> ${screenReaderActionText}</span>
          </a>
        </dd>
      </#if>
  </div>
</#macro>


