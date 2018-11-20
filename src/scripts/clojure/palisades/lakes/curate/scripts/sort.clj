(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-18"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj > sort.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
(doseq [dir [#_"Pictures" "photo"]]
  (let [drive (if (.exists (io/file "e:/")) "e:/" "s:/")
        ;;d0 (io/file drive "photo" "dxo-small")
        d0 (io/file drive dir)
        d1 (io/file drive "pic")]
    (doseq [f0 (curate/image-file-seq d0)]
      (curate/rename-image f0 d1))))
;;----------------------------------------------------------------
