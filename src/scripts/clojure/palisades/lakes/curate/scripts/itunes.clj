(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.itunes
  
  {:doc "de-dupe music files."
   :author "palisades dot lakes at gmail dot com"
   :version "2021-11-11"}
  
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

;; clj src\scripts\clojure\palisades\lakes\curate\scripts\itunes.clj
;;----------------------------------------------------------------
(defn- dup?
  "Does the filename end in \" 1.m4a\"?"
  [^java.io.File f]
  (let [n (.getName f)]
    (when (or 
            (s/ends-with? n " 1.m4a")
            (s/ends-with? n " 2.m4a")
            (s/ends-with? n " 3.m4a")
            (s/ends-with? n " 4.m4a")
            (s/ends-with? n " 5.m4a")
            )
      (let [p (.getPath f)
            l (.length p)
            f0 (io/file (str (.substring p 0 (- l 6)) ".m4a"))
            e (and (.exists f0) (= (.length f) (.length f0)))]
        #_(when e (println p) (println (.getPath f0)) (println))
        e))))  
;;----------------------------------------------------------------
(defn- dup-files 
  "Search recursively for files that end in \" 1.m4a\"."
  [^java.io.File d]
  (assert (.exists d) (.getPath d))
  (filter dup? (file-seq d)))  
;;----------------------------------------------------------------
(with-open [w (io/writer (str "itunes.txt"))]
  (binding [*out* w]
    (let [dups (dup-files (io/file "s:/Music"))]
      (println (count dups) (count (file-seq (io/file "s:/Music"))))
      (doseq [f dups]
        (io/delete-file f)))))
;;----------------------------------------------------------------
