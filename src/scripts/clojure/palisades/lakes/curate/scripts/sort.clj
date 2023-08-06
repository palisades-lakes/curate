(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2023-08-02"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate])
  (:import [java.time LocalDate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
(defn sort-images [^clojure.lang.IFn tester ^java.io.File d1]
  (with-open [w (io/writer (io/file d1 "sort.txt"))]
    (binding [*out* w]
      (doseq [dir [#_"a1/2023-06-w4"
                   #_"a1/2023-07-w123"
                   #_"a7c/2023-07"
                   "a7c/2023-08"
                   ;; note need to double underscores in filenames
                   #_"iphone14/202307__"
                   #_"iphone14/202308__"
                   "iphone14"
                   #_"Pictures"
                   #_"portfolio"]]
        (let [^java.io.File d0 (io/file "Z:/"  dir)]
          (if (.exists d0)
            (println "new"
                     (reduce
                       +
                       (map (fn ^long [^java.io.File f0]
                              (curate/rename-image f0 tester d1 false))
                            (curate/image-file-seq d0))))
            (println "doesn't exist" (.getPath d0)))))
      )))
;;----------------------------------------------------------------
(let [tester (curate/after-date? (LocalDate/of 2023 8 1))]
  (sort-images tester (io/file "Z:/" "sorted"))
  (sort-images tester (io/file "Y:/" "selecting")))

