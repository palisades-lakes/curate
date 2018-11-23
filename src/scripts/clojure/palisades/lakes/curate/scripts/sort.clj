(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-22"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj > sort.txt 
;;----------------------------------------------------------------
;; TODO: search all drives?
;;(let [drive (if (.exists (io/file "e:/")) "e:/" "s:/")
;;(doseq [drive ["f:/" "g:/" "y:/" "z:/"]]
(doseq [drive ["z" "l" "m"
               ;;"e" "f" "g" "h" "j" "k" "y"
               ]]
  (with-open [w (io/writer (str "sort-" drive ".txt"))]
    (binding [*out* w]
      (doseq [dir ["desktops"
                   "exif"
                   "photo-2014-04"
                   "porta/Pictures" 
                   "porta/Pictures-Tamaki" 
                   "porta/photo"
                   "archive/93c3z01/Pictures"
                   "archive/cl56/Pictures"
                   "silver-wdbig/archive/93c3z01/Pictures"
                   "silver-wdbig/archive/cl56/Pictures"
                   "silver-wdbig//porta/Pictures" 
                   "silver-wdbig/porta/Pictures-Tamaki" 
                   "silver-wdbig/snapshots/h/Pictures"
                   "Pictures" 
                   "Pictures-Tamaki" 
                   "photo"
                   ]]
        (let [d0 (io/file (str drive ";/") dir)
              d1 (io/file "e:/" "pic")]
          (when (.exists d0)
            (doseq [f0 (curate/image-file-seq d0)]
              (curate/rename-image f0 d1))))))))
;;----------------------------------------------------------------
