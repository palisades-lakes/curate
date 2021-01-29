(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2021-01-28"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir [
;                 "2019-10"
;                 "2019-11"
;                 "2019-12"
;                 "2020-01"
;                 "2020-02"
;                 "2020-03"
;                 "2020-04"
;                 "2020-05"
;                 "2020-06"
;                 "2020-07"
                 "2020-09"
                 "2021-01"
                 ]]
      (let [^java.io.File d0 (io/file "e:/" "Pictures" dir)
            ^java.io.File d1 (io/file "e:/" "pic")]
        (if (.exists d0)
          (doseq [^java.io.File f0 (curate/image-file-seq d0)]
            (println (.getName f0))
            (curate/rename-image f0 d1))
          (println "doesn't exist" (.getPath d0)))))))
;;----------------------------------------------------------------
