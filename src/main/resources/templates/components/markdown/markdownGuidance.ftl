<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Markdown" pageHeading="Markdown" fullWidthColumn=true>

  <h2 class="govuk-heading-l">What is markdown?</h2>
  <p class="govuk-body">Markdown is a 'markup' language which is designed to be as easy-to-read and
    easy-to-write as possible, using simple punctuation instead of complicated tags and code.</p>

  <h2 class="govuk-heading-l">Using markdown to format text</h2>

    <@headings />

    <@paragraphs />

    <@boldText />

    <@italics />

    <@bulletedLists />

    <@links />

</@defaultPage>

<#macro headings>
    <#outputformat "HTML">
        <#assign htmlExample>
          <h2 class="govuk-heading-l">This is an H2 subheading</h2>
          <h3 class="govuk-heading-m">This is an H3 subheading</h3>
        </#assign>
    </#outputformat>

    <#assign markdownExample>
## This is an H2 subheading
### This is an H3 subheading
    </#assign>

    <#assign codeLanguages = {
      "Markdown": markdownExample
    }>

  <h3 class="govuk-heading-m">Headings</h3>

  <p class="govuk-body">You can create heading levels using the hash character (#). The number of hashes shows the heading level.</p>
  <p class="govuk-body">This is how they look:</p>

  <@fdsCodeSample.renderedCodeSample id="headings-markdown-example" languageTabs=codeLanguages>
      ${htmlExample}
  </@fdsCodeSample.renderedCodeSample>

  <p class="govuk-body">Don’t skip heading levels - in other words, don’t jump straight to an H3. Don’t insert an H1 anywhere.</p>

</#macro>

<#macro paragraphs>

  <#outputformat "HTML">
      <#assign htmlExample>
        <p class="govuk-body">An empty line underneath text</p>
        <p class="govuk-body">will add a paragraph.</p>
      </#assign>
  </#outputformat>

  <#assign markdownExample>
An empty line underneath text

will add a paragraph.
  </#assign>

  <#assign codeLanguages = {
    "Markdown": markdownExample
  }>

  <h3 class="govuk-heading-m">Paragraphs</h3>

  <p class="govuk-body">You can create paragraphs by leaving an empty line between the end of one paragraph and the start of another.</p>
  <p class="govuk-body">This is how they look:</p>

  <@fdsCodeSample.renderedCodeSample id="paragraphs-markdown-example" languageTabs=codeLanguages>
      ${htmlExample}
  </@fdsCodeSample.renderedCodeSample>

</#macro>

<#macro boldText>

    <#outputformat "HTML">
        <#assign htmlExample>
          <strong>Double asterisks around text</strong> will turn it bold.
        </#assign>
    </#outputformat>

    <#assign markdownExample>
      **Double asterisks around text** will turn it bold.
    </#assign>

    <#assign codeLanguages = {
      "Markdown": markdownExample
    }>

  <h3 class="govuk-heading-m">Bold text</h3>

  <@fdsCodeSample.renderedCodeSample id="bold-text-markdown-example" languageTabs=codeLanguages>
      ${htmlExample}
  </@fdsCodeSample.renderedCodeSample>

</#macro>

<#macro italics>

    <#outputformat "HTML">
        <#assign htmlExample>
          <em>Underscores around text</em> will turn it italic.
        </#assign>
    </#outputformat>

    <#assign markdownExample>
      _Underscores around text_ will turn it italic.
    </#assign>

    <#assign codeLanguages = {
    "Markdown": markdownExample
    }>

  <h3 class="govuk-heading-m">Italic text</h3>

    <@fdsCodeSample.renderedCodeSample id="italic-text-markdown-example" languageTabs=codeLanguages>
        ${htmlExample}
    </@fdsCodeSample.renderedCodeSample>

</#macro>

<#macro bulletedLists>

    <#outputformat "HTML">
        <#assign htmlExample>
          <ul class="govuk-list govuk-list--bullet">
            <li>asterisk 1</li>
            <li>asterisk 2</li>
            <li>asterisk 3</li>
          </ul>
        </#assign>
    </#outputformat>

    <#assign markdownExample>
* asterisk 1
* asterisk 2
* asterisk 3

- a hyphen
- another hyphen

+ plus signs
+ more plus signs
    </#assign>

    <#assign codeLanguages = {
    "Markdown": markdownExample
    }>

  <h3 class="govuk-heading-m">Bulleted lists</h3>
  <p class="govuk-body">Create bulleted lists using asterisks, hyphens or plus signs at the start of each line:</p>

    <@fdsCodeSample.renderedCodeSample id="bulleted-lists-markdown-example" languageTabs=codeLanguages>
        ${htmlExample}
    </@fdsCodeSample.renderedCodeSample>

</#macro>

<#macro links>

    <#outputformat "HTML">
        <#assign htmlExample>
          <a class="govuk-link govuk-link--no-visited-state" href="https://www.gov.uk">Welcome to GOV.UK</a>
        </#assign>
    </#outputformat>

    <#assign markdownExample>
      [https://www.gov.uk](Welcome to GOV.UK)
    </#assign>

    <#assign codeLanguages = {
    "Markdown": markdownExample
    }>

  <h3 class="govuk-heading-m">Links</h3>
  <p class="govuk-body">The link text goes in square brackets, and the URL goes in standard brackets, with no spaces in between:</p>

    <@fdsCodeSample.renderedCodeSample id="links-text-markdown-example" languageTabs=codeLanguages>
        ${htmlExample}
    </@fdsCodeSample.renderedCodeSample>

</#macro>