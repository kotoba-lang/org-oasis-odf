(ns odf.core-test
  (:require [clojure.test :refer [deftest is]]
            [odf.core :as odf]))

(defn- e [name s] {:name name :bytes (mapv int (.getBytes ^String s "UTF-8"))})

(deftest odf-parse
  (let [content "<office:document-content><office:body><office:text><text:p>Hello <text:span>odf</text:span></text:p><text:p>Line two</text:p></office:text></office:body></office:document-content>"
        entries [(e "mimetype" "application/vnd.oasis.opendocument.text")
                 (e "content.xml" content)]
        p (odf/parse entries)]
    (is (= :odt (:kind p)))
    (is (= ["Hello odf" "Line two"] (:paragraphs p)))))
