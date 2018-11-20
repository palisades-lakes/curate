(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-20"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj > sort.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
;;(let [drive (if (.exists (io/file "e:/")) "e:/" "s:/")
;;(doseq [drive ["f:/" "g:/" "y:/" "z:/"]]
(doseq [drive ["e:/"]]
  (doseq [dir ["Pictures" "Pictures-Tamaki" "photo"]]
    (let [d0 (io/file drive dir)
          d1 (io/file "e:/" "pic")]
      (when (.exists d0)
        (doseq [f0 (curate/image-file-seq d0)]
          (curate/rename-image f0 d1))))))
;;----------------------------------------------------------------
