<#include '../layout.ftl'>

<#-- @ftlvariable name="createPwaApplicationUrl" type="String" -->
<#-- @ftlvariable name="workAreaUrl" type="String" -->
<#-- @ftlvariable name="applicationTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<@defaultPage htmlTitle="Start PWA application">

    <@fdsError.errorSummary errorItems=errorList/>
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
                <p class="govuk-body">All new fields irrespective of pipeline lengths including 28-day public notice. Note this also includes cases where there are Median Line implications.</p>
              <#elseif appTypeName == "DEPOSIT_CONSENT">
                <p class="govuk-body">For any deposits being laid to support or protect a pipeline which has been authorised under a PWA</p>
              <#elseif appTypeName == "CAT_1_VARIATION">
                <p class="govuk-body">Varying an existing PWA and any new pipeline being installed in the Variation work scope is more than 500m in length and outside an HSE recognised safety zone. This also requires a 28 day Public Notice.</p>
              <#elseif appTypeName == "CAT_2_VARIATION">
                <h4 class="govuk-heading-s">Installation or Variation</h4>
                <p class="govuk-body">Where any pipeline is less than 500m in length or totally within a HSE recognised safety zone or varying an existing pipeline in the PWA Regime.</p>
                <h4 class="govuk-heading-s">Removal or Out of Use</h4>
                <p class="govuk-body">Where an existing pipeline within the PWA Regime is to be partially or fully removed from the seabed or taken out of use. This is prior to agreement of COP approval.</p>
              <#elseif appTypeName == "HUOO_VARIATION">
                <p class="govuk-body">The Holder must make an application to <a href="mailto:${contactEmail!""}">${contactEmail!""}</a> very early in the process regarding any proposed changes to the Holder, User, Operator or Owner information for OGA’s consideration using the HUOO template. If the OGA is content with the proposed changes the OGA will advise the Holder to resubmit the application nearer the execution date. The actual consent will not be issued until the deed has been executed.</p>
              <#elseif appTypeName == "OPTIONS_VARIATION">
                <p class="govuk-body">Where the problem with a pipeline(s) may not be clearly identified and there may be various points of possible failure, OGA may consider an Options case. To apply under the above circumstances, the Holder should email <a href="mailto:${contactEmail!""}">${contactEmail!""}</a> detailing why they would like OGA to consider the case to be handled as an Options.</p>
              <#elseif appTypeName == "DECOMMISSIONING">
                <p class="govuk-body">For proposed subsea pipeline works associated with Decommissioning- only to be used on the condition that OGA has agreed COP and the Operator has confirmed the last day of production in writing to OGA Operations</p>
              </#if>
              </p>
            </@fdsRadio.radioItem>

          </#list>
      </@fdsRadio.radioGroup>
      <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workAreaUrl)/>

    </@fdsForm.htmlForm>

</@defaultPage>