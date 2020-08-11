<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="permanentDeposits">${sectionDisplayText}</h2>
    <#list diffedDepositList as deposit>
        <@diffedDepositViewSummary deposit=deposit/>
    </#list>

</div>

<#macro diffedDepositViewSummary deposit>
  <#if deposit.PermanentDepositOverview_depositReference.diffType == "DELETED">
    <#local diffHideGroup = "hide-when-diff-disabled"/>
  </#if>

  <dl class="govuk-summary-list govuk-!-margin-bottom-9 ${diffHideGroup!}">
    <h3 class="govuk-heading-m"><@diffChanges.renderDiff deposit.PermanentDepositOverview_depositReference/></h3>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Pipelines</dt>
      <dd class="govuk-summary-list__value">
        <ol class="govuk-list">
          <#list deposit.PermanentDepositOverview_pipelineRefs as pipelineRef>
            <li><@diffChanges.renderDiff pipelineRef/></li>
          </#list>
        </ol>

      </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Proposed start date</dt>
      <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_fromDateEstimate/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">End date</dt>
      <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_toDateEstimate/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Type of materials</dt>
      <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_materialType/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Size</dt>
      <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_materialSize/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Quantity</dt>
      <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_quantity/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Contingency included</dt>
      <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_contingencyAmount/></dd>
    </div>

    <#local showGroutBagFields=("GROUT_BAGS"==deposit.PermanentDepositOverview_materialTypeLookup.currentValue! || "GROUT_BAGS"==deposit.PermanentDepositOverview_materialTypeLookup.previousValue!)/>
    <#if showGroutBagFields>
      <div class="govuk-summary-list__row">
        <dt class="govuk-summary-list__key">Biodegradable grout bags used</dt>
        <dd class="govuk-summary-list__value"><@diffChanges.renderDiff deposit.PermanentDepositOverview_groutBagsBioDegradable/></dd>
      </div>
      <div class="govuk-summary-list__row">
        <dt class="govuk-summary-list__key">Non-biodegradable grout bags used reason</dt>
        <dd class="govuk-summary-list__value">
          <@diffChanges.renderDiff diffedField=deposit.PermanentDepositOverview_bioGroutBagsNotUsedDescription multiLineTextBlockClass="govuk-summary-list__value"/>
        </dd>
      </div>
    </#if>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From (WGS84)</dt>
      <dd class="govuk-summary-list__value">
        <@diffChanges.renderDiff diffedField=deposit.PermanentDepositOverview_fromCoordinates/>
      </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To (WGS84)</dt>
      <dd class="govuk-summary-list__value">
        <@diffChanges.renderDiff diffedField=deposit.PermanentDepositOverview_toCoordinates/>
      </dd>
    </div>
  </dl>
</#macro>