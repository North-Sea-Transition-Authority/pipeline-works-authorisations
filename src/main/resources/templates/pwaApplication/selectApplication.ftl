<#include '../layout.ftl'>

<#-- @ftlvariable name="createPwaApplicationUrl" type="String" -->
<#-- @ftlvariable name="workAreaUrl" type="String" -->
<#-- @ftlvariable name="applicationTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<@defaultPage htmlTitle="Select application type" errorItems=errorList backLink=true>

    <@fdsForm.htmlForm>

      <@fdsRadio.radioGroup
          labelText="What type of application do you want to start?"
          path="form.applicationType"
          hiddenContent=true
          fieldsetHeadingClass="govuk-fieldset__legend--l"
          fieldsetHeadingSize="h1">

          <#list applicationTypes as appType>
            <#assign appTypeName = appType.name()/>
            <#assign displayName = appType.displayName/>
            <@fdsRadio.radioItem path="form.applicationType" itemMap={appTypeName: displayName}>

              <#if appTypeName == "INITIAL">
                <p class="govuk-body">${initialGuideText}This requires a 28 day Public Notice. This also includes cases where there are Median Line implications.</p>
              <#elseif appTypeName == "DEPOSIT_CONSENT">
                <p class="govuk-body">For any deposits being laid to support or protect a pipeline which has been authorised under a PWA</p>
              <#elseif appTypeName == "CAT_1_VARIATION">
                <p class="govuk-body">Varying an existing PWA where any new pipeline being installed in the Variation work scope is more than 500m in length and outside an HSE recognised safety zone. This requires a 28 day Public Notice.</p>
              <#elseif appTypeName == "CAT_2_VARIATION">
                <p class="govuk-body">A category 2 variation is used when varying an existing PWA for the following reasons:</p>
                <ul class="govuk-list govuk-list--bullet">
                  <li>adding new pipelines less than 500m in length or totally within a HSE recognised safety zone</li>
                  <li>varying an existing pipeline in the PWA Regime</li>
                  <li>partially or fully removing an existing pipeline within the PWA Regime from the seabed or taking it out of use prior to agreement of Cessation of Production approval</li>
                  <li>bringing a pipeline into the PWA Regime that already exists on the seabed</li>
                </ul>
              <#elseif appTypeName == "HUOO_VARIATION">
                <p class="govuk-body">For any changes to the current holder, user, operator or owner information only.</p>
              <#elseif appTypeName == "OPTIONS_VARIATION">
                <p class="govuk-body">Where the problem with a pipeline(s) may not be clearly identified and there may be various points of possible failure, NSTA may consider an Options case. To apply under the above circumstances, the Holder should provide details to why they would like NSTA to consider the case to be handled as an Options.</p>
              <#elseif appTypeName == "DECOMMISSIONING">
                <p class="govuk-body">For proposed subsea pipeline works associated with Decommissioning. This is only to be used on the condition that NSTA has agreed Cessation of Production and the operator has confirmed the last day of production in writing to NSTA Operations.</p>
              </#if>
            </@fdsRadio.radioItem>

          </#list>
      </@fdsRadio.radioGroup>
      <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workAreaUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>