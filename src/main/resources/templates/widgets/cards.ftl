<#--Cards-->
<#macro card>
  <div class="card">
    <#nested>
  </div>
</#macro>

<#macro cardHeader cardHeading="" cardHeadingSize="h4" cardHeadingClass="govuk-heading-m">
  <div class="card__header">
    <#if cardHeading?has_content>
      <${cardHeadingSize} class="${cardHeadingClass}">${cardHeading}</${cardHeadingSize}>
    </#if>
    <div class="card__actions">
      <#nested>
    </div>
  </div>
</#macro>

<#macro cardAction cardLinkText cardLink="#">
  <a href="${cardLink}" class="govuk-link card__link">${cardLinkText}</a>
</#macro>

<#macro cardFilesList cardFilesHeading="govuk-heading-s" cardFilesHeadingSize="h4">
  <${cardFilesHeadingSize} class="${cardFilesHeading}">Attached files</${cardFilesHeadingSize}>
  <ul class="card__files-list">
    <#nested>
  </ul>
</#macro>

<#macro cardFilesListItem fileDescription>
  <li class="card__files-list-item">
    <#nested>
    <p class="govuk-body">${fileDescription}</p>
  </li>
</#macro>