(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns 
  ^{:doc "compute a file checksum."}
  
  curate.scripts.checksum
  
  (:require [clojure.java.io :as io])
  (:use [clojure.set :only [difference]])
  (:gen-class))
;;------------------------------------------------------------------------------
(defn checksum [file]
  (let [input (java.io.FileInputStream. file)
        digest (java.security.MessageDigest/getInstance "MD5")
        stream (java.security.DigestInputStream. input digest)
        bufsize (* 1024 1024)
        buf (byte-array bufsize)]

  (while (not= -1 (.read stream buf 0 bufsize)))
  (apply str (map (partial format "%02x") (.digest digest)))))

(defn list-dir [dir]
  (remove #(.isDirectory %)
          (file-seq (java.io.File. dir))))

(defn find-dupes [root]
  (let [files (list-dir root)]
    (let [summed (zipmap (pmap #(checksum %) files) files)]
      (difference
       (into #{} files)
       (into #{} (vals summed))))))

(defn remove-dupes [files]
  (prn "Duplicates files to be removed:")
  (doseq [f files] (prn (.toString f)))
  (prn "Delete files? [y/n]:")
  (if-let [choice (= (read-line) "y")]
    (doseq [f files] 
      (println (.getName ^java.io.File f))
      #_(.delete f))))

(defn -main [& args]
  (if (empty? args)
    (println "Enter a root directory")
    (remove-dupes (find-dupes (first args))))
  (System/exit 0))
;;------------------------------------------------------------------------------
