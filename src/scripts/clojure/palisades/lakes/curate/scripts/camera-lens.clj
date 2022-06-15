(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.camera-lens
  
  {:doc "unique camera lens combinations."
   :author "palisades dot lakes at gmail dot com"
   :version "2022-06-14"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\camera-lens.clj > camera-lens.txt 
;;----------------------------------------------------------------
(let [d (io/file "z:/"
                 #_"a1"
                 #_"resolved")]
  (pp/pprint
    (sort-by 
      first
      (into #{}
            (map (fn [f] 
                   [(curate/exif-camera f) 
                    (curate/exif-lens f)])
                 (curate/image-file-seq d))))))
  #_(let [d (io/file "z:/"
                     #_"a1"
                     #_"resolved")]
      (doall
        (map 
          (fn [^String k]
            (println (str  k ":"))
            (pp/pprint
              (sort-by first
                       (into #{}
                             (map (fn [f] 
                                    [(curate/exif-camera f) 
                                     (curate/exif-value k f)])
                                  (curate/image-file-seq d)))
                       )
              )
            (println))
          [#_"Conversion Lens"
           #_"Infinity Lens Step"
           #_"Near Lens Step"
           "Lens" 
           #_"Lens Auto-Focus Stop Button Function Switch"
           #_"Lens Data"
           #_"Lens Distortion Parameters"
           #_"Lens Firmware Version"
           #_"Lens Format"
           #_"Lens ID"
           #_"Lens Info Array"
           #_"Lens Make"
           "Lens Model"
           #_"Lens Mount"
           #_"Lens Properties"
           #_"Lens Range"
           #_"Lens Serial Number"
           #_"Internal Lens Serial Number"
           #_"Lens Spec"
           #_"Lens Specification"
           #_"Lens Spec Features"
           #_"Lens Stops"
           #_"Lens Temperature"
           #_"Lens Type"
           #_"Lens Type 2"
           ])))
  ;;----------------------------------------------------------------
  