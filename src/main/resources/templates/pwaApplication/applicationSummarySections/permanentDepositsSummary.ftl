<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="permanentDeposits">${sectionDisplayText}</h2>
    <#list depositList as deposit>
        <@depositViewSummary deposit=deposit/>
    </#list>

</div>

<#macro depositViewSummary deposit>

  <dl class="govuk-summary-list govuk-!-margin-bottom-9">
    <h3 class="govuk-heading-m">${deposit.depositReference}</h3>

    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Is the deposit for a consented pipeline or a pipeline that is on this application?</dt>
      <dd class="govuk-summary-list__value">
        <#if deposit.depositIsForConsentedPipeline?has_content>${deposit.depositIsForConsentedPipeline?then('Yes', 'No')}</#if>
      </dd>
    </div>
    
    <#if deposit.depositIsForConsentedPipeline?has_content && deposit.depositIsForConsentedPipeline == true>
      <div class="govuk-summary-list__row">
        <dt class="govuk-summary-list__key">Pipelines</dt>
        <dd class="govuk-summary-list__value">
          <ol class="govuk-list">
            <#list deposit.pipelineRefs as pipelineRef>
              <li>${pipelineRef}</li>
            </#list>
          </ol>

        </dd>
      </div>
    </#if>

    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Is the deposit for proposed pipelines on other applications that haven’t yet been consented?</dt>
      <dd class="govuk-summary-list__value">
         <#if deposit.depositIsForPipelinesOnOtherApp?has_content>${deposit.depositIsForPipelinesOnOtherApp?then('Yes', 'No')}</#if>
      </dd>
    </div>

    <#if deposit.depositIsForPipelinesOnOtherApp?has_content && deposit.depositIsForPipelinesOnOtherApp == true>
      <div class="govuk-summary-list__row">
        <dt class="govuk-summary-list__key">Application reference and proposed pipeline number of each pipeline</dt>
        <dd class="govuk-summary-list__value">
          ${deposit.appRefAndPipelineNum!}
        </dd>
      </div>
    </#if>

    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Proposed start date</dt>
      <dd class="govuk-summary-list__value">${deposit.fromDateEstimate} </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">End date</dt>
      <dd class="govuk-summary-list__value">${deposit.toDateEstimate} </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Type of materials</dt>
      <dd class="govuk-summary-list__value">${deposit.materialType.value} </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Size</dt>
      <dd class="govuk-summary-list__value">${deposit.materialSize} </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Quantity</dt>
      <dd class="govuk-summary-list__value">${deposit.quantity} </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Contingency included</dt>
      <dd class="govuk-summary-list__value">${deposit.contingencyAmount!} </dd>
    </div>

    <#local showGroutBagFields=("GROUT_BAGS"==deposit.materialTypeLookup.currentValue! || "GROUT_BAGS"==deposit.materialTypeLookup.previousValue!)/>
    <#if showGroutBagFields>
      <div class="govuk-summary-list__row">
        <dt class="govuk-summary-list__key">Biodegradable grout bags used</dt>
        <dd class="govuk-summary-list__value">${deposit.groutBagsBioDegradable} </dd>
      </div>
      <div class="govuk-summary-list__row">
        <dt class="govuk-summary-list__key">Non-biodegradable grout bags used reason</dt>
        <dd class="govuk-summary-list__value">
          ${deposit.bioGroutBagsNotUsedDescription}
        </dd>
      </div>
    </#if>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From (WGS84)</dt>
      <dd class="govuk-summary-list__value">
        <@pwaCoordinate.display coordinatePair=deposit.fromCoordinates />
      </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To (WGS84)</dt>
      <dd class="govuk-summary-list__value">
        <@pwaCoordinate.display coordinatePair=deposit.toCoordinates />
      </dd>
    </div>
  </dl>
</#macro>