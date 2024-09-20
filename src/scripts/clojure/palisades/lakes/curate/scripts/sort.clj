(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2024-03-02"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate])
  (:import [java.time LocalDate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
(defn sort-images [^clojure.lang.IFn tester ^java.io.File d1]
  (let [logfile (io/file d1 "sort.txt")]
    (io/make-parents logfile)
    (with-open [w (io/writer logfile)]
      (binding [*out* w]
        (doseq [dir ["a1"
                     "a7c"
                     "iphone14"
                     #_"Pictures"
                     #_"portfolio"]]
          (let [^java.io.File d0 (io/file "Z:/"  dir)]
            (if (.exists d0)
              (println "new"
                       (reduce
                         + (map (fn ^long [^java.io.File f0]
                                (curate/rename-image f0 tester d1 false))
                              (curate/image-file-seq d0))))
              (println "doesn't exist" (.getPath d0)))))))))
;;----------------------------------------------------------------
(let [tester (curate/after-date? (LocalDate/of 2024 9 3))]
  (sort-images tester (io/file "Z:/" "sorted"))
  (sort-images tester (io/file "Y:/" "selecting")))
