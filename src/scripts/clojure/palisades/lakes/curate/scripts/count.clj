(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.count
  
  {:doc "count image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-12"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [palisades.lakes.curate.curate :as curate]))
;;----------------------------------------------------------------
(let [d (io/file "s:/" "porta" #_"Pictures")]
  (println (curate/unix-path d))
  (println (count (curate/image-file-seq d))))
;;----------------------------------------------------------------
