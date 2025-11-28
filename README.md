# PRINT OPEN HTML TO PDF

![PDF screenshot of OpenHTMLtoPDF](screenshot.png)

## OVERVIEW
This is a fork of the [OpenHTMLtoPDF project](https://github.com/danfickle/openhtmltopdf). The fork was carried out in 2025.

Print Open HTML to PDF is a pure-Java library for rendering a reasonable subset of well-formed XML/XHTML (and even some HTML5)
using CSS 2.1 (and later standards) for layout and formatting, outputting to PDF or images.

Use this library to generated nice looking PDF documents. But be aware that you can not throw modern HTML5+ at
this engine and expect a great result. You must special craft the HTML document for this library and 
use it's extended CSS feature like [#31](https://github.com/danfickle/openhtmltopdf/pull/31) or
[#32](https://github.com/danfickle/openhtmltopdf/pull/32) 
to get good results. Avoid floats near page breaks and use table layouts.

Open HTML to PDF uses a couple of FOSS packages to get the job done. A list
of these can be found in the [dependency graph](https://github.com/danfickle/openhtmltopdf/network/dependencies).

## GETTING STARTED
+ [Integration guide](https://github.com/danfickle/openhtmltopdf/wiki/Integration-Guide) - get maven artifacts and code to get started.
    + Note: This documentation is hosted on the old GitHub repository
+ [1.0.10 Online Sandbox](https://sandbox.openhtmltopdf.com/) - Now with logs!
    + Note: This sandbox is hosted from the origin author of the project
+ [Templates for Openhtmltopdf](https://danfickle.github.io/pdf-templates/index.html) - MIT licensed templates that work with this project. Updated 2021-09-21.
    + Note: These templates are hosted from the origin author of the project
+ [Showcase Document - PDF](https://openhtmltopdf.com/showcase.pdf)
    + Note: This showcase document is hosted from the origin author of the project
+ [Documentation wiki](https://github.com/danfickle/openhtmltopdf/wiki)
    + Note: This documentation is hosted on the old GitHub repository
+ [Sample Project - Pretty Resume Generator](https://github.com/danfickle/pretty-resume)
    + Note: This sample project is hosted from the origin author of the project

## DIFFERENCES WITH FLYING SAUCER
+ Uses the well-maintained and open-source (LGPL compatible) PDFBOX as PDF library, rather than iText.
+ Proper support for generating accessible PDFs (Section 508, PDF/UA, WCAG 2.0).
+ Proper support for generating PDF/A standards compliant PDFs.
+ New, faster renderer means this project can be several times faster for very large documents.
+ Better support for CSS3 transforms.
+ Automatic visual regression testing of PDFs, with many end-to-end tests.
+ Ability to insert pages for cut-off content.
+ Built-in plugins for SVG and MathML.
+ Font fallback support.
+ Limited support for RTL and bi-directional documents.
+ On the negative side, no support for OpenType fonts.
+ Footnote support.
+ Much more. See changelog below.

## CREDITS
Thanks to @danfickle for his work advancing the OpenHtmlToPdf project beyond FlyingSaucer.

Open HTML to PDF is based on [Flying-saucer](https://github.com/flyingsaucerproject/flyingsaucer). Credit goes to the contributors of that project. Code will also be used from [neoFlyingSaucer](https://github.com/danfickle/neoflyingsaucer)

## FAQ
+ OPEN HTML TO PDF is tested with OpenJDK 8, 11 and 17 (early access). It requires at least Java 8 to run.
+ No, you can not use it on Android.
+ You should be able to use it on Google App Engine (Java 8 or greater environment). [Let us know your experience](https://github.com/danfickle/openhtmltopdf/issues/179).
+ <s>Flowing columns are not implemented.</s> Implemented in RC12.
+ No, it's not a web browser. Specifically, it does not run javascript or implement many modern standards such as flex and grid layout.

## TEST CASES
Test cases, failing or working are welcome, please place them
in ````/openhtmltopdf-examples/src/main/resources/testcases/````
and run them
from ````/openhtmltopdf-examples/src/main/java/com/openhtmltopdf/testcases/TestcaseRunner.java````.

## CHANGELOG

### 0.2.2

+ Fix: NPE in COSArray.add with big HTML document in PDF A_1_A mode and Arial font. The fix is suggested by a Pull Request from [CedricMtta](https://github.com/danfickle/openhtmltopdf/commit/b88d40a5aba6b85e76be843acd071844a011fb7c)
+ Allow to tag header and footer elements as suggested from [danfickle](https://github.com/danfickle/openhtmltopdf/commit/a068c4034fdcba92b99f740dc39f7c7e37ac590e)
+ Update groupIds & artifactIds to reflect new project naming
+ Update README to reflect new project naming & fork definition

### OLDER RELEASES

[View CHANGELOG.md](CHANGELOG.md).