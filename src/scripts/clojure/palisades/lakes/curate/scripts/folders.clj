(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.folders
  
  {:doc "list new folders."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-12"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [exif-processor.core :as exif]
            [palisades.lakes.curate.curate :as curate])
  (:import [java.io File]))
;; clj9 src\scripts\clojure\palisades\lakes\curate\scripts\folders.clj > folders.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
(let [drive (if (.exists (io/file "e:/")) "e:/" "s:/")
      d0 (io/file drive "porta" "Pictures")
      d1 (io/file drive "porta" "pic")]
  (pp/pprint
    (into (sorted-set)
          (map #(curate/unix-path 
                  (.getParentFile (curate/new-path % d1))))
          (curate/image-file-seq d0))))
;;----------------------------------------------------------------
