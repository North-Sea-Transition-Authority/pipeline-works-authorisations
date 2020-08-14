<#import '/spring.ftl' as spring>
<#include '../../layout.ftl'>

<#-- @ftlvariable name="taskName" type="java.lang.String" -->
<#-- @ftlvariable name="route" type="java.lang.String" -->
<#-- @ftlvariable name="isCompleted" type="java.lang.Boolean" -->
<#-- @ftlvariable name="taskInfoList" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo>" -->

<#macro taskInfoItem taskName taskInfoList route isCompleted>

  <li class="fds-task-list__item">
    <span class="fds-task-list__task-name">
      <a class="govuk-link" href="${springUrl(route)}">
        ${taskName}
      </a>
    </span>
      <#list taskInfoList as taskInfo>
          <#if taskInfo.count gt 0>
            <strong class="govuk-tag fds-task-list__task-completed">
                <@stringUtils.pluralise count=taskInfo.count word=taskInfo.countType />
            </strong>
          </#if>
      </#list>
      <#if isCompleted>
        <strong class="govuk-tag fds-task-list__task-completed">COMPLETED</strong>
      <#else>
        <strong class="govuk-tag govuk-tag--grey">NOT COMPLETED</strong>
      </#if>
  </li>
</#macro>