(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.images
  
  {:doc "find image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-06"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [palisades.lakes.curate.curate :as curate]))
;;----------------------------------------------------------------
(let [d (io/file "e:/" "porta" #_"Pictures")
      filetypes (into (sorted-set)
                      (map curate/file-type)
                      (file-seq d))]
  (pp/pprint filetypes)
  (pp/pprint (remove curate/image-file-type? filetypes))
  (pp/pprint (filter curate/image-file-type? filetypes)))
#_(let [d (io/file "e:/" "porta" #_"Pictures")]
    (pp/pprint
      (into (sorted-set)
            (map curate/file-type)
            (curate/image-file-seq d))))
;;----------------------------------------------------------------
