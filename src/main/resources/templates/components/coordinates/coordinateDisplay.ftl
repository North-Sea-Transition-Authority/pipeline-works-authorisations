<#-- Viewing coordinates -->

<#-- @ftlvariable name="coordinatePair" type="uk.co.ogauthority.pwa.model.location.CoordinatePair" -->

<#macro display coordinatePair>

    <#if coordinatePair.latitude.degrees?has_content>
        ${coordinatePair.latitude.degrees} &deg; ${coordinatePair.latitude.minutes}' ${coordinatePair.latitude.seconds}" ${coordinatePair.latitude.direction?substring(0,1)}
      <br/>
        ${coordinatePair.longitude.degrees} &deg; ${coordinatePair.longitude.minutes}' ${coordinatePair.longitude.seconds}" ${coordinatePair.longitude.direction?substring(0,1)}
        <#else>
        <p class="govuk-body">No coordinates specified</p>
    </#if>

</#macro>