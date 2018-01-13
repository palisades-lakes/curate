(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.cameras
  
  {:doc "unique cameras."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-12"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [exif-processor.core :as exif]
            [palisades.lakes.curate.curate :as curate])
  (:import [java.io File]))
;; clj9 src\scripts\clojure\palisades\lakes\curate\scripts\cameras.clj > cameras.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
(let [drive (if (.exists (io/file "e:/")) "e:/" "s:/")
      d (io/file drive "porta" "Pictures")]
  (pp/pprint
    (into (sorted-set)
          (map curate/image-file-camera)
          (curate/image-file-seq d))))
;;----------------------------------------------------------------
