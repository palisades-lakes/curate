(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.exif
  
  {:doc "experiment wtih exif."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-11-29"}
  
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]
            [exif-processor.core :as exif])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata Directory Metadata Tag]
           [com.drew.metadata.exif
            ExifIFD0Directory ExifSubIFDDirectory]))
;; clj src\scripts\clojure\palisades\lakes\curate\scripts\exif.clj > exif.txt
;; java -jar lib\metadata-extractor-2.11.0.jar e:\photo\dxo-out\2013-02-19-14-56-00-NEX-7.jpg -markdown -hex > metadata-sony-dxo.md
;; java -jar lib\metadata-extractor-2.11.0.jar e:\photo\original\2013-02-19-14-56-00-NEX-7.arw -hex > metadata-sony.txt
;; java -jar lib\metadata-extractor-2.11.0.jar e:\photo\dxo-out\2013-02-19-14-56-00-NEX-7.jpg -hex > metadata-sony-dxo.txt
;; java -jar lib\metadata-extractor-2.11.0.jar "e:\photo\dxo-out\2004-07-28-23-43-52-Canon PowerShot A80.jpg" -hex > metadata-canon-dxo.txt
;;----------------------------------------------------------------
(let [root (io/file "e:/")
      dxo (io/file root "photo" "dxo-out")
      f (io/file dxo "2013-02-19-14-56-00-NEX-7.jpg")
      ^Metadata m (try (ImageMetadataReader/readMetadata f)
                    (catch Throwable t
                      (binding [*err* *out*]
                        (stacktrace/print-cause-trace t))
                      (throw t)))
      ^Iterable ds (.getDirectories m)
      exif (exif/exif-for-file f)]
  (println :exif)
  (println "Software" (get exif "Software"))
  (pp/pprint exif)
  (println "----------------------------------------")
  (println "----------------------------------------")
  (println :metadata)
  (doseq [^Directory d (seq ds)]
    #_(println)
    (println "----------------------------------------")
    (println :directory (class d) (.getName d) (.getTagCount d))
    (doseq [^Tag tag (.getTags d)]
      (println (.getTagTypeHex tag) (.getTagName tag) 
               ":" (.getDescription tag))
      (println (.toString tag))))
  #_(let [^ExifSubIFDDirectory d 
          (.getFirstDirectoryOfType m ExifSubIFDDirectory)
          date 
          (.getDate d ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
      (println date))
  )
;;----------------------------------------------------------------
