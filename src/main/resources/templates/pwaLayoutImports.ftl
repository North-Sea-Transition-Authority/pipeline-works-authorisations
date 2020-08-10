<#import '/spring.ftl' as spring/>

<#--Layout-->
<#include 'fds/objects/layouts/generic.ftl'>
<#import 'fds/objects/grid/grid.ftl' as grid>
<#import 'header.ftl' as pipelinesHeader>
<#import 'components/fileUpload/fileUpload.ftl' as fileUpload>
<#import 'components/completedTag/completedTag.ftl' as completedTag>
<#import 'components/coordinates/locationInput.ftl' as pwaLocationInput>
<#import 'components/coordinates/coordinateDisplay.ftl' as pwaCoordinate>
<#import 'components/taskList/pwaTaskListItem.ftl' as pwaTaskListItem>
<#import 'components/cartographic/maps.ftl' as maps>
<#import 'components/utils/string.ftl' as stringUtils>
<#import 'components/widgets/pipelineTableSelection.ftl' as pwaPipelineTableSelection/>
<#import 'components/widgets/organisationUnitDetailTableSelection.ftl' as pwaOrgDetailTableSelection/>
<#import 'components/utils/validationResult.ftl' as validationResult/>
<#import 'components/diff/diffChanges.ftl' as diffChanges/>
<#import 'components/utils/multiLineText.ftl' as multiLineText>

<#function springUrl url>
    <#local springUrl>
        <@spring.url url/>
    </#local>
    <#return springUrl>
</#function>
