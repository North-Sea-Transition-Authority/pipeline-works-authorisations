<#include '../../../layout.ftl'>

<#-- @ftlvariable name="changeHoldersUrl" type="String" -->

<@defaultPage htmlTitle="Pipeline organisations changes" pageHeading="Pipeline organisations changes" fullWidthColumn=true>

  <table class="govuk-table">
    <caption class="govuk-table__caption govuk-table__caption-l">
      Holders
      <@fdsAction.link linkText="Change" linkUrl=springUrl(changeHoldersUrl) linkClass="govuk-link govuk-!-font-size-19" />
    </caption>
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Pipeline number</th>
      <th class="govuk-table__header" scope="col">Ident number</th>
      <th class="govuk-table__header" scope="col">Previous holders</th>
      <th class="govuk-table__header" scope="col">New holders</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list 1..6 as i>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">PL1</td>
        <td class="govuk-table__cell">${i}</td>
        <td class="govuk-table__cell">
          <div class="diff-changes diff-changes--flex">
            <del class="diff-changes__delete">
              <span class="diff-changes__value">Royal Dutch Shell</span>
            </del>
          </div>
        </td>
        <td class="govuk-table__cell">
          <div class="diff-changes diff-changes--flex">
            <ins class="diff-changes__insert">
              <span class="diff-changes__value">BP Exploration</span>
            </ins>
          </div>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <hr class="govuk-section-break">

  <table class="govuk-table">
    <caption class="govuk-table__caption govuk-table__caption-l">
      Users
      <@fdsAction.link linkText="Change" linkUrl="#" linkClass="govuk-link govuk-!-font-size-19" />
    </caption>
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Pipeline number</th>
      <th class="govuk-table__header" scope="col">Ident number</th>
      <th class="govuk-table__header" scope="col">Previous users</th>
      <th class="govuk-table__header" scope="col">New users</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list 1..6 as i>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">PL1</td>
        <td class="govuk-table__cell">${i}</td>
        <td class="govuk-table__cell">
          Conocophillips
          <br/>
          <div class="diff-changes diff-changes--flex">
            <del class="diff-changes__delete">
              <span class="diff-changes__value">Taqa Brittani</span>
            </del>
          </div>
        </td>
        <td class="govuk-table__cell">
          Conocophillips
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <hr class="govuk-section-break">

  <table class="govuk-table">
    <caption class="govuk-table__caption govuk-table__caption-l">
      Operators
      <@fdsAction.link linkText="Change" linkUrl="#" linkClass="govuk-link govuk-!-font-size-19" />
    </caption>
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Pipeline number</th>
      <th class="govuk-table__header" scope="col">Ident number</th>
      <th class="govuk-table__header" scope="col">Previous operators</th>
      <th class="govuk-table__header" scope="col">New operators</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list 1..6 as i>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">PL1</td>
        <td class="govuk-table__cell">${i}</td>
        <td class="govuk-table__cell">
          Wintershall BV
        </td>
        <td class="govuk-table__cell">
          Wintershall BV
          <br/>
          <div class="diff-changes diff-changes--flex">
            <ins class="diff-changes__insert">
              <span class="diff-changes__value">Repsol Sinopec</span>
            </ins>
          </div>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <hr class="govuk-section-break">

  <table class="govuk-table">
    <caption class="govuk-table__caption govuk-table__caption-l">
      Owners
      <@fdsAction.link linkText="Change" linkUrl="#" linkClass="govuk-link govuk-!-font-size-19" />
    </caption>
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="col">Pipeline number</th>
      <th class="govuk-table__header" scope="col">Ident number</th>
      <th class="govuk-table__header" scope="col">Previous owners</th>
      <th class="govuk-table__header" scope="col">New owners</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list 1..6 as i>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">PL1</td>
        <td class="govuk-table__cell">${i}</td>
        <td class="govuk-table__cell">
          <div class="diff-changes diff-changes--flex">
            <del class="diff-changes__delete">
              <span class="diff-changes__value">GASSCO AS</span>
            </del>
          </div>
        </td>
        <td class="govuk-table__cell">
          <div class="diff-changes diff-changes--flex">
            <ins class="diff-changes__insert">
              <span class="diff-changes__value">ZENNOR PETROLEUM</span>
            </ins>
          </div>
          <div class="diff-changes diff-changes--flex">
            <ins class="diff-changes__insert">
              <span class="diff-changes__value">4GEN OPERATING LIMITED</span>
            </ins>
          </div>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>

  <hr class="govuk-section-break">

  <@fdsForm.htmlForm>
      <@fdsAction.button buttonText="Save and continue" />
  </@fdsForm.htmlForm>

</@defaultPage>
