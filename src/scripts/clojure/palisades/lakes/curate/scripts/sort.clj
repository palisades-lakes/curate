(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-25"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))
;; clj9 src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj > sort.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
(let [drive (if (.exists (io/file "e:/")) "e:/" "s:/")
      d0 (io/file drive "photo" "dxo-small")
      ;;d0 (io/file drive "porta" "Pictures")
      d1 (io/file drive "pic")]
  (doseq [f0 (curate/image-file-seq d0)]
    (curate/rename-image f0 d1)))
;;----------------------------------------------------------------
