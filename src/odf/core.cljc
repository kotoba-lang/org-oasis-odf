(ns odf.core
  "OpenDocument Format (OASIS ODF, also ISO/IEC 26300) content extraction
   from an already-unzipped entry table (.odt/.ods/.odp). Detects the
   document type from the `mimetype` entry and extracts text:p paragraph
   text from content.xml. Pure cljc, zero dependencies — the caller unzips
   the ODF file (e.g. with org-pkware-zip) and passes the resulting entries
   here. Extracted from kotoba-lang/kasane (kasane.normalize/odf->doc,
   ADR-2606272100) as `org-oasis-odf`."
  (:require [clojure.string :as str]))

(defn- bytes->str [bs] (apply str (map char bs)))

(defn- xml-texts [xml tag]
  (mapv second (re-seq (re-pattern (str "<" tag "[^>]*>([\\s\\S]*?)</" tag ">")) xml)))

(defn- strip-tags [s]
  (-> (str s) (str/replace #"<[^>]*>" " ") (str/replace #"\s+" " ") str/trim))

(defn parse
  "`entries` = seq of {:name :bytes} (an already-unzipped ODF package).
   Returns {:kind :odt|:ods|:odp|:odf :paragraphs [\"...\" ...]}."
  [entries]
  (let [by   (into {} (map (juxt :name identity) entries))
        mime (some-> (by "mimetype") :bytes bytes->str)
        kind (cond (and mime (str/includes? mime "spreadsheet"))  :ods
                   (and mime (str/includes? mime "presentation")) :odp
                   (and mime (str/includes? mime "text"))         :odt
                   :else :odf)
        texts (mapv strip-tags (xml-texts (or (some-> (by "content.xml") :bytes bytes->str) "") "text:p"))]
    {:kind kind :entry-count (count entries) :paragraphs texts}))
