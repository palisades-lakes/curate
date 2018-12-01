(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.processor
  
  {:doc "collect software tags."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-30"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\processor.clj > processor.txt 
;;----------------------------------------------------------------
(let [d (io/file "e:/" "pic")]
  (pp/pprint
    (sort
      (into 
        #{} 
        (map curate/exif-processor (curate/image-file-seq d))))))
;;----------------------------------------------------------------
