<#import '/spring.ftl' as spring/>

<#--Layout-->
<#include 'fds/objects/layouts/generic.ftl'>
<#import 'fds/objects/grid/grid.ftl' as grid>
<#import 'header.ftl' as pipelinesHeader>
<#import 'components/flash/flash.ftl' as pwaFlash>
<#import 'components/fileUploadList/fileList.ftl' as pwaFiles>
<#import 'components/completedTag/completedTag.ftl' as completedTag>
<#import 'components/coordinates/locationInput.ftl' as pwaLocationInput>
<#import 'components/coordinates/coordinateDisplay.ftl' as pwaCoordinate>
<#import 'components/taskList/pwaTaskListItem.ftl' as pwaTaskListItem>
<#import 'components/cartographic/maps.ftl' as maps>
<#import 'components/utils/string.ftl' as stringUtils>
<#import 'components/widgets/pipelineTableSelection.ftl' as pwaPipelineTableSelection>
<#import 'components/widgets/organisationUnitDetailTableSelection.ftl' as pwaOrgDetailTableSelection>
<#import 'components/utils/validationResult.ftl' as validationResult>
<#import 'components/diff/diffChanges.ftl' as diffChanges>
<#import 'components/utils/multiLineText.ftl' as multiLineText>
<#import 'components/caseSummary/caseSummary.ftl' as pwaCaseSummary>
<#import 'components/clauseList/clauseList.ftl' as pwaClauseList>
<#import 'components/appUpdateRequest/updateRequestView.ftl' as pwaUpdateRequestView/>
<#import 'components/banner/pageBanner.ftl' as pageBanner/>
<#import 'components/appSummary/appSummary.ftl' as pwaAppSummary>
<#import 'components/sidebarSectionLink/sidebarSectionLink.ftl' as pwaSidebarSectionLink>
<#import 'components/tableSelectorToggler/tableSelectorToggler.ftl' as pwaTableSelectionToggler>
<#import 'components/payment/payment.ftl' as pwaPayment>
<#import 'components/mailMerge/mailMergeFieldList.ftl' as pwaMailMerge>
<#import 'components/hiddenInput/hiddenInput.ftl' as pwaHiddenInput>
<#import 'components/banner/notificationBanner.ftl' as notificationBanner>
<#import 'components/markdown/markdownInsetText.ftl' as pwaMarkdownInsetText>
<#import 'components/minMaxSummary/minMaxSummary.ftl' as minMaxSummary>

<#function springUrl url>
    <#local springUrl>
        <@spring.url url/>
    </#local>
    <#return springUrl>
</#function>

<#-- Constructs path of complex form inputs when nested within a list -->
<#function createNestedFormPath listPath listItemIndex listItemAttribute>
    <#local combinedPath="${listPath}[${listItemIndex}].${listItemAttribute}"/>
    <#return combinedPath>
</#function>

<#macro pwaCustomScripts>
    <script src="<@spring.url '/assets/static/js/pwa/tableSelectionToggler.js'/>"></script>
    <script src="<@spring.url'/assets/static/js/pwa/googleAnalyticsEventTracking.js'/>"></script>
    <script>
        var PWA_CONFIG = {
            analyticsMeasurementUrl: "<@spring.url analyticsMeasurementUrl/>",
            globalTag: "${analytics.globalTag}"
        };
    </script>
</#macro>