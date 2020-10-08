<#-- Viewing coordinates -->

<#-- @ftlvariable name="coordinatePair" type="uk.co.ogauthority.pwa.model.location.CoordinatePair" -->

<#macro display coordinatePair>

    <#if coordinatePair?has_content && coordinatePair.latitude.degrees?has_content>
        ${coordinatePair.latitude.getDisplayString()}
      <br/>
        ${coordinatePair.longitude.getDisplayString()}
    </#if>

</#macro>