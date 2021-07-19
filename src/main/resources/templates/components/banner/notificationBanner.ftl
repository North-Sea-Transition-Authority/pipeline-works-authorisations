<#include '../../layout.ftl'>

<#-- @ftlvariable name="notificationBannerView" type="uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerView" -->

<#macro infoNotificationBanner notificationBannerView>

    <#if notificationBannerView.title?hasContent>
        <@fdsNotificationBanner.notificationBannerInfo bannerTitleText=notificationBannerView.title>
            <@fdsNotificationBanner.notificationBannerContent>

                <#list notificationBannerView.bodyLines as bodyLine>
                    <p class="${bodyLine.lineClass!"govuk-body"}">${bodyLine.lineText}</p>
                </#list>

            </@fdsNotificationBanner.notificationBannerContent>
        </@fdsNotificationBanner.notificationBannerInfo>
    </#if>

</#macro>