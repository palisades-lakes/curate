(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.dxo
  
  {:doc "detect dxo processed files."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-29"}
  
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]
            [exif-processor.core :as exif])
  (:import [java.io File]
           [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata Directory Metadata Tag]
           [com.drew.metadata.exif
            ExifIFD0Directory ExifSubIFDDirectory]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\dxo.clj > dxo.txt
;;----------------------------------------------------------------
(defn- find-software [^File f]
  (let [^Metadata m (try (ImageMetadataReader/readMetadata f)
                      (catch Throwable t
                        (binding [*err* *out*]
                          (stacktrace/print-cause-trace t))
                        (throw t)))]
    #_(println :exif)
    #_(println "Software" (get (exif/exif-for-file f) "Software"))
    #_(println "----------------------------------------")
    #_(println :metadata)
    (doseq [^Directory d (seq (.getDirectories m))]
      #_(println)
      (println "----------------------------------------")
      (println :directory (class d))
      (println (.getName d))
      (println (.getTagCount d))
      #_(println (.toString d))
      (let [^Directory parent (.getParent d)]
        (println :parent (when parent (.toString parent))))
      (doseq [^Tag tag (.getTags d)]
        (println (.getTagTypeHex tag) ":" (.getTagName tag) 
                 ":" (.getDescription tag))
        #_(println (.toString tag))))
    ))
;;----------------------------------------------------------------
(let [folder (io/file "e:/" "photo" )
      fname "2013-02-19-14-56-00-NEX-7"
      original (io/file folder "original" (str fname ".arw"))
      dxo (io/file folder "dxo-out" (str fname ".jpg"))]
  (doseq [^File f [original dxo]]
    (println "========================================")
    (println (.getPath f))
    (find-software f)
    (println) (println)))
;;----------------------------------------------------------------
