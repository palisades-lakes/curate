(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort0
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2022-03-14"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort0.clj
;;----------------------------------------------------------------
;; sort into year/month folders
;;----------------------------------------------------------------
(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir [#_"Pictures"
                 #_"photo"
                 "a7c/2022"
                 "a1/2022"
                 "iphone"
                 ]]
      (let [^java.io.File d0 (io/file "z:/"  dir)
            ^java.io.File d1 (io/file "z:/" "sorted")]
        (if (.exists d0)
          (doseq [^java.io.File f0 (curate/image-file-seq d0)]
            #_(println (.getName f0))
            (curate/rename-image-year-month f0 d1 false))
          (println "doesn't exist" (.getPath d0)))))
    (doseq [dir ["pic"
                 ]]
      (let [^java.io.File d0 (io/file "z:/"  dir)
            ^java.io.File d1 (io/file "z:/" "sorted")]
        (if (.exists d0)
          (doseq [^java.io.File f0 (curate/image-file-seq d0)]
            #_(println (.getName f0))
            (curate/rename-image-year-month f0 d1 true))
          (println "doesn't exist" (.getPath d0)))))))
;;----------------------------------------------------------------
