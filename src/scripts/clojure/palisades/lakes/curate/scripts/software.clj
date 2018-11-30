(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.software
  
  {:doc "collect software tags."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-29"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            #_[exif-processor.core :as exif]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\software.clj > software.txt 
;;----------------------------------------------------------------
(let [d (io/file "e:/" "pic")]
  (pp/pprint
    (into (sorted-set)
          (mapcat curate/exif-software
                  (curate/image-file-seq d)))))
;;----------------------------------------------------------------
