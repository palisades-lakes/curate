(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.sort
  
  {:doc "rename and de-dupe image files."
   :author "palisades dot lakes at gmail dot com"
   :version "2022-12-13"}
  
  (:require [clojure.java.io :as io]
            [palisades.lakes.curate.curate :as curate]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\sort.clj
;;----------------------------------------------------------------
(with-open [w (io/writer (str "sort.txt"))]
  (binding [*out* w]
    (doseq [dir ["a1"
                 "a7c"
                 #_"iphone"
                 #_"Pictures"
                 #_"portfolio"]]
      (let [^java.io.File d0 (io/file "z:/"  dir)
            ^java.io.File d1 (io/file "z:/" "sorted")]
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
