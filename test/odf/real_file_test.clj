(ns odf.real-file-test
  "odf.core/parse against a real pandoc-generated .odt (`pandoc book.md -o
   book.odt`). The existing odf-parse test only ever exercised a hand-built
   2-entry vector; this is the first real ODF package (real
   META-INF/manifest.xml, real content.xml with pandoc's actual
   text:style-name/paragraph conventions, real styles.xml) this parser has
   ever seen. This test does its own unzip (java.util.zip, JVM-only) since
   odf.core is a zero-dep pure parser that takes already-unzipped
   {:name :bytes} entries by design (the caller is expected to supply a zip
   reader, e.g. org-pkware-zip, at the call site)."
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [odf.core :as odf])
  (:import [java.util.zip ZipInputStream]))

(defn- unzip-resource [path]
  (with-open [zis (ZipInputStream. (io/input-stream (io/resource path)))]
    (loop [acc []]
      (if-let [ent (.getNextEntry zis)]
        (let [baos (java.io.ByteArrayOutputStream.)]
          (io/copy zis baos)
          (recur (conj acc {:name (.getName ent)
                             :bytes (mapv #(bit-and (int %) 0xff) (.toByteArray baos))})))
        acc))))

(deftest real-pandoc-odt
  (let [entries (unzip-resource "odf/fixtures/pandoc_book.odt")
        p (odf/parse entries)]
    (testing "mimetype entry -> :odt kind detection against pandoc's real package"
      (is (= :odt (:kind p))))
    (is (= 6 (:entry-count p)))
    (testing "text:p paragraph extraction from pandoc's real content.xml
              (chapter headings use text:h, not text:p, so only title/
              author/body paragraphs appear here -- correct per parser scope)"
      (is (= ["My Real Book" "Real Author"
              "Hello epub world from a real pandoc export."
              "Second chapter content here."]
             (:paragraphs p))))))
