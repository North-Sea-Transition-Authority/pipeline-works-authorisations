<#-- Viewing coordinates -->
<#import '../utils/numberPad.ftl' as numberPad>

<#-- @ftlvariable name="coordinatePair" type="uk.co.ogauthority.pwa.model.location.CoordinatePair" -->

    <#macro pad value>
        <@numberPad.leftPad value=value leftPadAmount=2 leftPadCharacter="0"/>
    </#macro>

<#macro display coordinatePair>

    <#if coordinatePair?has_content && coordinatePair.latitude.degrees?has_content>
        <@pad value=coordinatePair.latitude.degrees/> ° <@pad value=coordinatePair.latitude.minutes/>' <@pad value=coordinatePair.latitude.seconds/>" ${coordinatePair.latitude.direction?substring(0,1)}
      <br/>
        <@pad value=coordinatePair.longitude.degrees/> ° <@pad value=coordinatePair.longitude.minutes/>' <@pad value=coordinatePair.longitude.seconds/>" ${coordinatePair.longitude.direction?substring(0,1)}
    </#if>

</#macro>