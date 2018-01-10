(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.scripts.exif
  
  {:doc "experiment wtih exif."
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-09"}
  
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]
            [exif-processor.core :as exif])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata Directory Metadata Tag]
           [com.drew.metadata.exif
            ExifIFD0Directory ExifSubIFDDirectory]))
;; clj9 src\scripts\clojure\palisades\lakes\curate\scripts\exif.clj > exif.txt
;;----------------------------------------------------------------
(let [
      ;      root (io/file "f:\\")
      ;      porta (io/file root "porta" "Pictures")
      ;      f0 (io/file porta "sony-n7-2016-11-29" "10060724"  
      ;                  "DSC06137.ARW")
      ;      f1 (io/file porta "iphone6sp-2016-11-29" "IMG_0541.JPG")
      ;      good (io/file root "photo" "good")
      ;      f2 (io/file good 
      ;                  "2004-12-23-06-55-56-Canon PowerShot A95.jpg")
      ;      f3 (io/file good "2010-11-09-21-57-20-NEX-5.JPG")
      ;      dxo (io/file root "photo" "dxo-out")
      ;      f4 (io/file dxo "2010-11-09-21-57-20-NEX-5.JPG")
      ;      small (io/file root "photo" "dxo-small")
      ;      f5 (io/file small "2010-11-09-21-57-20-NEX-5.JPG")
      f (io/file #_"s:\\porta\\Pictures\\1-12-2013\\DSC02199.ARW"
                 #_"s:/porta/Pictures/2004_06_12/husky-stadium.jpg"
                 #_"s:/porta/Pictures/2004-12-23-tamaki-canon/tamaki-0001.JPG"
                 "s:/porta/Pictures/galaxy5 1971-06-25/1062_211960.jpg")
      ^Metadata m 
      (try (ImageMetadataReader/readMetadata f)
        (catch Throwable t
          (binding [*err* *out*] (stacktrace/print-cause-trace t))
          (throw t)))
      ^Iterable ds (.getDirectories m)
      exif (exif/exif-for-file f)]
  (doseq [^Directory d (seq ds)]
    (println)
    #_(println "----------------------------------------")
    (println (class d) (.getName d) (.getTagCount d))
    (doseq [^Tag tag (.getTags d)]
      (println (.getTagTypeHex tag) (.getTagName tag) 
               ":" (.getDescription tag))
      (println (.toString tag))))
  #_(let [^ExifSubIFDDirectory d 
         (.getFirstDirectoryOfType m ExifSubIFDDirectory)
         date 
         (.getDate d ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
     (println date))
  (pp/pprint exif))
;;----------------------------------------------------------------
