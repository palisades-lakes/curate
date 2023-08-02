(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2023-08-02"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir [#_"a1/2023-06-w4"
                 #_"a1/2023-07-w123"
                 "a7c/2023-07"
                 ;; note need to double underscores in filenames
                 "iphone14/202307__"
                 "iphone14/202308__"
                 #_"Pictures"
                 #_"portfolio"]]
      (let [^java.io.File d0 (io/file "Z:/"  dir)
            ^java.io.File d1 (io/file "Z:/" "sorted")]
        (if (.exists d0)
          (println "new"
                   (reduce 
                     + 
                     (map (fn ^long [^java.io.File f0] 
                            (curate/rename-image f0 d1 false))
                          (curate/image-file-seq d0))))
          (println "doesn't exist" (.getPath d0)))))
    ))
;;----------------------------------------------------------------
