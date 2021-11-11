(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2021-03-15"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir ["a1"
                 "a7c"
                 "Pictures"
                 ]]
      (let [^java.io.File d0 (io/file "z:/"  dir)
            ^java.io.File d1 (io/file "z:/" "sorted")]
        (if (.exists d0)
          (doseq [^java.io.File f0 (curate/image-file-seq d0)]
            (println (.getName f0))
            (curate/rename-image f0 d1))
          (println "doesn't exist" (.getPath d0)))))))
;;----------------------------------------------------------------
#_(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir [
                 "2021-02"
                 "2021-03"
                 ]]
      (let [^java.io.File d0 (io/file "z:/" "Pictures" dir)
            ^java.io.File d1 (io/file "e:/" "pic")]
        (if (.exists d0)
          (doseq [^java.io.File f0 (curate/image-file-seq d0)]
            (println (.getName f0))
            (curate/rename-image f0 d1))
          (println "doesn't exist" (.getPath d0)))))))
;;----------------------------------------------------------------
