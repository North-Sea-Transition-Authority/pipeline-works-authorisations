<#import '/spring.ftl' as spring>
<#include '../../layout.ftl'>

<#-- @ftlvariable name="taskName" type="String" -->
<#-- @ftlvariable name="taskInfo" type="uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo" -->

<#macro taskInfoItem taskName taskInfo>
  <li class="fds-task-list__item">
    <span class="fds-task-list__task-name">
      <a class="govuk-link" href="${springUrl(taskInfo.link)}">
        ${taskName}
      </a>
    </span>
    <#if taskInfo.count gt 0>
      <strong class="govuk-tag fds-task-list__task-completed">${taskInfo.count + ' ' + taskInfo.countType}</strong>
    </#if>
  </li>
</#macro>