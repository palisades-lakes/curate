(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.datetimes
  
  {:doc "find image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-06"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [exif-processor.core :as exif]
            [palisades.lakes.curate.curate :as curate])
  (:import [java.io File]))
;;----------------------------------------------------------------
(let [d (io/file "e:/" "porta" "Pictures")]
  (group-by 
    curate/image-file-datetime
    (take 10000 (curate/image-file-seq d)))
  nil)
;;----------------------------------------------------------------
