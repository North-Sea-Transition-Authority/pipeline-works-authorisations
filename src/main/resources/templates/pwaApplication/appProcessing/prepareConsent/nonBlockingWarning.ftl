<#include '../../../layout.ftl'>

<#macro nonBlockingWarningBanner nonBlockingTasksWarning>
  <#if nonBlockingTasksWarning.tasksHaveWarnings>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing tasks">
      <@fdsNotificationBanner.notificationBannerContent headingText=nonBlockingTasksWarning.incompleteTasksWarningText>
        ${nonBlockingTasksWarning.incompleteTasksWarningText}. You can continue to send for approval or go back to  
        <@fdsAction.link linkText="case management" linkUrl=springUrl(nonBlockingTasksWarning.returnUrl)/> to start the tasks.
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
