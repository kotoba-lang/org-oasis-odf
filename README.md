# kotoba-lang/org-oasis-odf

Zero-dep portable `.cljc` OpenDocument Format (OASIS ODF, also ISO/IEC
26300) content extractor for `.odt`/`.ods`/`.odp`. Detects document kind
from the `mimetype` entry and extracts `text:p` paragraph text from
`content.xml`. Operates on an already-unzipped entry table (`{:name
:bytes}` seq) — the caller unzips the ODF file first, e.g. with
`org-pkware-zip`. Extracted from `kotoba-lang/kasane`
(kasane.normalize/odf->doc, ADR-2606272100).

## Usage

```clojure
(require '[odf.core :as odf])

(odf/parse entries)  ; entries = seq of {:name "path" :bytes [...]}
;; => {:kind :odt|:ods|:odp|:odf :entry-count N :paragraphs ["..." ...]}
```

## Test

```sh
clojure -M:test
```
