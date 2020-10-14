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