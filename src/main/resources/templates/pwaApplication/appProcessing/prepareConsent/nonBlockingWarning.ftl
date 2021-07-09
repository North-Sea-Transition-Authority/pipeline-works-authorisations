<#include '../../../layout.ftl'>

<#macro nonBlockingWarningBanner nonBlockingTasksWarning>
  <#if nonBlockingTasksWarning.tasksHaveWarnings>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing tasks">
      <@fdsNotificationBanner.notificationBannerContent headingText=nonBlockingTasksWarning.incompleteTasksWarningText>
        <#if nonBlockingTasksWarning.returnMessage?has_content>
          <#assign returnMessage =  nonBlockingTasksWarning.returnMessage/>
          ${returnMessage.messagePrefix}
          <@fdsAction.link linkText=returnMessage.urlLinkText linkUrl=springUrl(returnMessage.returnUrl)/> 
          ${returnMessage.messageSuffix!}        
        </#if>
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
