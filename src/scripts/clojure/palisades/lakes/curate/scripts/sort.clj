(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2019-01-10"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
;; TODO: search all drives?
(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir [#_"2018-12"
                 "2019-01"
                 #_"iphone6splus-20181203"
                 #_"nex7-20181203"
                 #_"nex5-20181129"]]
      (let [^java.io.File d0 (io/file "e:/" "Pictures" dir)
            ^java.io.File d1 (io/file "e:/" "pic")]
        (if (.exists d0)
          (doseq [^java.io.File f0 (curate/image-file-seq d0)]
            (println (.getName f0))
            (curate/rename-image f0 d1))
          (println "doesn't exist" (.getPath d0)))))))
#_(with-open [w (io/writer (str "sort.txt"))]
    (binding [*out* w]
      (doseq [d ["pic-20181129" "nex5-20181129" "nex7-20181130"]]
        (let [d0 (io/file "q:/" d)
              d1 (io/file "e:/" "pic")]
          (when (.exists d0)
            (doseq [f0 (curate/image-file-seq d0)]
              (curate/rename-image f0 d1)))))))
#_(doseq [drive ["n" 
                 ;;"z" "l" "m"
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
          (let [d0 (io/file (str drive ":/") dir)
                d1 (io/file "e:/" "pic")]
            (when (.exists d0)
              (doseq [f0 (curate/image-file-seq d0)]
                (curate/rename-image f0 d1))))))))
;;----------------------------------------------------------------
