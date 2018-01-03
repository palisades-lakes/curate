(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns curate.scripts.readexif
  (:require [clojure.java.io :as io])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata Directory Metadata Tag]
           [com.drew.metadata.exif ExifIFD0Directory ExifSubIFDDirectory]))
;;------------------------------------------------------------------------------
(let [root (io/file "f:\\")
      porta (io/file root "porta" "Pictures")
      f0 (io/file porta "sony-n7-2016-11-29" "10060724"  "DSC06137.ARW")
      f1 (io/file porta "iphone6sp-2016-11-29" "IMG_0541.JPG")
      good (io/file root "photo" "good")
      f2 (io/file good "2004-12-23-06-55-56-Canon PowerShot A95.jpg")
      f3 (io/file good "2010-11-09-21-57-20-NEX-5.JPG")
      dxo (io/file root "photo" "dxo-out")
      f4 (io/file dxo "2010-11-09-21-57-20-NEX-5.JPG")
      small (io/file root "photo" "dxo-small")
      f5 (io/file small "2010-11-09-21-57-20-NEX-5.JPG")
      ^Metadata m (ImageMetadataReader/readMetadata f5)
      ^Iterable ds (.getDirectories m)]
  (doseq [^Directory d (seq ds)]
    (println)
    #_(println "----------------------------------------")
    (println (class d) (.getName d) (.getTagCount d))
    (doseq [^Tag tag (.getTags d)]
      (println (.getTagTypeHex tag) (.getTagName tag) ":" (.getDescription tag))
      (println (.toString tag))))
  (let [^ExifSubIFDDirectory d (.getFirstDirectoryOfType m ExifSubIFDDirectory)
        date (.getDate d ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
    (println date)))
;;------------------------------------------------------------------------------
