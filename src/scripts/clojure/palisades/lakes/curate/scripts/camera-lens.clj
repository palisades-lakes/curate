(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.camera-lens
  
  {:doc "unique camera lens combinations."
   :author "palisades dot lakes at gmail dot com"
   :version "2022-06-08"}
  
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\camera-lens.clj > camera-lens.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
(let [d (io/file "z:/"
                 #_"a1"
                 "resolved")]
  (doall
    (map 
      (fn [^String k]
        (println (str  k ":"))
        (pp/pprint
          ;;(sort
            (into #{}
                  (map (fn [f] 
                         [(curate/exif-camera f) 
                          (curate/exif-value k f)])
                       (curate/image-file-seq d)))
            ;;)
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
       "Lens ID"
       #_"Lens Info Array"
       "Lens Make"
       "Lens Model"
       #_"Lens Mount"
       #_"Lens Properties"
       #_"Lens Range"
       #_"Lens Serial Number"
       #_"Internal Lens Serial Number"
       "Lens Spec"
       "Lens Specification"
       #_"Lens Spec Features"
       #_"Lens Stops"
       #_"Lens Temperature"
       "Lens Type"
       #_"Lens Type 2"
       ])))
;;----------------------------------------------------------------
