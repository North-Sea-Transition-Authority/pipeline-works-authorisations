# 0001: Document Generation System Overview
* Author(s): Harshid Dattani  
* Created Date: 05-12-2024  
* Last Updated Date: 05-12-2024
---

## Key Concepts and Components

### Document Specification and Template Mnemonics

**Enums** such as `DocumentTemplateMnem` and `DocumentSpec` define the high-level structure and purpose of each generated document:

- `DocumentTemplateMnem`: Represents a group of related document specifications by resource type.
  - For example:  `PETROLEUM_CONSENT_DOCUMENT` groups all specs related to Petroleum consent documents (e.g., `INITIAL_PETROLEUM_CONSENT_DOCUMENT`, `VARIATION_PETROLEUM_CONSENT_DOCUMENT`).  
- `DocumentSpec`: Defines a particular configuration of a document, including:  
  - Which **sections** the document will contain.  
  - The order in which those sections should appear.  
  - The associated application types (e.g., `INITIAL`, `VARIATION`, `DECOMMISSIONING`).

From a `PwaApplication` (which has a resource type and application type), the system determines the correct `DocumentSpec` to use. This links an application scenario (like an initial CCUS application) to a defined document layout and content set.

### Document Sections, Section Types, and Clause Display

`DocumentSection`, `SectionType`, and `ClauseDisplay` define the structure and rendering rules at a more granular level:

- `DocumentSection`: Enumerates individual sections of the document (e.g., `INITIAL_INTRO`, `INITIAL_TERMS_AND_CONDITIONS`, `DEPOSIT_DRAWINGS`, `TABLE_A`).  
  - Each section has a `SectionType` and a `ClauseDisplay`
  - Some sections have an associated `DocumentSectionGenerator` implementation that pulls or creates specific data (like charts, tables, or introduction paragraphs).  
- `SectionType`:  
  - `CLAUSE_LIST`: A section composed of editable clauses.  
  - `CUSTOM`: A section generated from custom logic in a service.  
  - `OPENING_PARAGRAPH`: Usually the initial paragraphs of a document. Adding and removing clauses is not allowed. Also generated from custom logic in a service.
- `ClauseDisplay` determines if the clause headings are visible or hidden when editing the template

### Templates vs. Instances

- **Document Templates** (represented by `DocumentTemplate` and associated `DocumentTemplateSection` and `DocumentTemplateSectionClause`):  
  - Define a "blueprint" for a document.  
  - Stored clauses and their versions represent a baseline set of content that can be used to generate actual documents.  
- **Document Instances** (represented by `DocumentInstance` and associated `DocumentInstanceSectionClause`):  
  - Created from templates for a specific `PwaApplication`.  
  - Contain clauses that may be edited or adjusted at the instance level.  
  - Represent the "final" or "application-specific" version of a document that can be turned into a PDF.

### Clauses and Clause Versions

Documents are made up of **sections**, and sections are made up of **clauses**. Clauses can be hierarchical (top-level and sub-clauses).

- **Template Clauses**: Represent baseline content. Stored as `DocumentTemplateSectionClause` and versioned in `DocumentTemplateSectionClauseVersion`.  
- **Instance Clauses**: Created from template clauses for a particular application's document instance. Stored as `DocumentInstanceSectionClause` and versioned in `DocumentInstanceSectionClauseVersion`.

Clauses can be added, edited, removed, or reordered at both the template and instance levels. Every time a clause is changed, a new "version" is created, allowing tracking of changes over time.

## Services

Multiple services coordinate the doc gen process:

- `DocumentTemplateService` and `TemplateDocumentSource`:  
  - Handles loading and managing template-level clauses and sections. Provides a `DocumentTemplateDto` that represents the complete template.

- `DocumentInstanceService`:  
  - Responsible for creating and managing `DocumentInstance` objects derived from templates. It handles operations such as adding/subtracting clauses, editing clauses, and retrieving instance-level `DocumentView` representations.

- `DocgenService` and `DocumentCreationService`:  
  - `DocgenService` schedules and processes 'docgen' (document generation) runs. A docgen run is a request to produce a finalized document (HTML and then PDF) for a given `DocumentInstance`.  
  - `DocumentCreationService` uses the specified `DocumentSpec` and the sections it defines to:  
    1. Retrieve or generate each section's content.  
    2. Merge them into HTML.  
    3. Apply mail merges (inserting application-specific data).  
    4. Render the final combined HTML to PDF.

- `SectionClauseCreator`, `DocumentClauseService`, `DocumentClauseFactory`:  
  - Assist with creating and manipulating clause structures for both templates and instances.

- `DocumentViewService`:  
  - Constructs a `DocumentView` object that provides a hierarchical structure of sections and clauses suitable for rendering. It also generates navigation links and ensures clauses and sections are in the correct order.

- `MailMergeService`:  
  - Performs the substitution of placeholders (mail merge fields) within clauses. Some fields are "automatic" (populated by system data), and some are "manual" (entered by users). It integrates with `MarkdownService` to convert Markdown to HTML and apply resolved mail merge fields.
  - To handle different kinds of data, **MailMergeService** can call multiple resolvers. Each resolver implements the `DocumentSourceMailMergeResolver` interface, which defines:
    - **Whether** it supports a given `DocumentSource` (like a PWA application or a template source).
    - **Which** mail merge fields apply to that source.
    - **How** those fields should be replaced with real content.

## Document Generation Flow

1. **Identify DocumentSpec**:  
   - Given a `PwaApplication`, the system finds the relevant `DocumentSpec` using `DocumentSpec.getSpecForApplication(...)`.
   
2. **Create or Load Document Instance**:  
   - If a document instance doesn't exist for the application and template mnemonic, it is created from the template (using `DocumentTemplateService` and `DocumentInstanceService`).

3. **Load Clauses and Create Document View**:  
   - For each section defined in the `DocumentSpec` (in the order specified):  
     - If it's a `CLAUSE_LIST` section, fetch clauses from the instance.  
     - If it's a `CUSTOM` section, invoke the appropriate `DocumentSectionGenerator` (like `InitialIntroductionGeneratorService`) to produce dynamic content.  
     - Build a `DocumentView` structure that contains all clauses and their hierarchy.

4. **Apply Mail Merge**:  
   - Use `MailMergeService` to substitute placeholders in the clauses and sections with actual data from the `PwaApplication` or other sources.

5. **Generate HTML and Combine Sections**:  
   - Render each section's content (clauses, tables, introductions, etc.) into HTML using `TemplateRenderingService`.

6. **Create the Final Document (PDF)**:  
   - Combine all rendered sections into a single HTML document and then pass it to the `PdfRenderingService` to produce a final PDF.

7. **Persist**:  
   - Save the generated PDF (in a `DocgenRun` record).