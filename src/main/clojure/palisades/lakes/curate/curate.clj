;;----------------------------------------------------------------
;; exif-processor has reflection and boxing warnings
(set! *warn-on-reflection* false)
(set! *unchecked-math* false)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.curate
  
  {:doc "photo curation utilities"
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-09"}
  
  (:require [clojure.set :as set]
            [clojure.string :as s]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.stacktrace :as stacktrace]
            [exif-processor.core :as exif])
  (:import [java.io File FileInputStream]
           [java.nio.file Files LinkOption]
           [java.nio.file.attribute FileTime]
           [java.security DigestInputStream MessageDigest]
           [java.time LocalDateTime ZoneOffset]
           [java.time.format DateTimeFormatter]
           [java.util Map]
           [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata Directory Metadata Tag]
           [com.drew.metadata.exif
            ExifIFD0Directory ExifSubIFDDirectory]))
;;----------------------------------------------------------------
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(defn print-exif [^File f]
  (let [^Metadata m 
        (try (ImageMetadataReader/readMetadata f)
          (catch Throwable t
            (binding [*err* *out*]
              (stacktrace/print-cause-trace t))
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
    (pp/pprint exif)))
;;----------------------------------------------------------------
(defn upathname 
  "return a unix style pathname string."
  ^String [^File f]
  (s/replace (.getPath f) "\\" "/"))
;;----------------------------------------------------------------
(defn file-type ^String [^File f]
  (let [filename (.getName f)
        i (s/last-index-of filename ".")
        ^String ext (if (nil? i)
                      ""
                      (s/lower-case (subs filename (inc (long i)))))]
    (if (>= 5 (.length ext) 2) ext "")))
;;----------------------------------------------------------------
;; https://en.wikipedia.org/wiki/Raw_image_format#Raw_filename_extensions_and_respective_camera_manufacturers
;; https://sno.phy.queensu.ca/~phil/exiftool/#supported
;; https://github.com/drewnoakes/metadata-extractor-images/wiki/ContentSummary
#_(def image-file-type?
    #{#_"3fr" ;; Hasselblad
      "3g2" "3gp"
      #_"ari" ;; Arri Alexa
      "arw" #_"srf" #_"sr2" ;; Sony
      #_"bay" ;; Casio
      "bmp" 
      #_"cri" ;; Cintel
      #_"crw" "cr2" ;; Canon
      #_"cap" #_"iiq" #_"eip" ;; Phase One
      #_"dcs" #_"dcr" #_"drf" #_"k25" #_"kdc" ;; Kodak
      "dng" ;; Adobe, Leica
      #_"erf" ;; Epson
      #_"fff" ;; Imacon/Hasselblad raw
      "gif" 
      "ico"
      "jpeg" "jpg" 
      "m4v"
      #_"mef" ;; Mamiya
      #_"mdc" ;; Minolta, Agfa
      #_"mos" ;; Leaf
      "mov"
      "mp4"
      #_"mrw" ;; Minolta, Konica Minolta
      "nef" #_"nrw" ;; Nikon
      "orf" ;; Olympus
      "pcx"
      #_"pef" #_"ptx" ;; Pentax
      "png" 
      "psd"
      #_"pxn" ;; Logitech
      #_"r3d" ;; RED Digital Cinema
      "raf" ;; Fuji
      "raw" ;; Panasonic, Leica
      "rw2" ;; Panasonic 
      "rwl" ;; Leica
      #_"rwz" ;; Rawzor
      "srw" ;; Samsung
      "tif" "tiff"
      "webp"
      "x3f" ;; Sigma
      })
(def image-file-type?
  #{"3fr" ;; Hasselblad
    #_"3g2" 
    #_"3gp"
    "ari" ;; Arri Alexa
    "arw" "srf" "sr2" ;; Sony
    "bay" ;; Casio
    "bmp" 
    "cri" ;; Cintel
    "crw" "cr2" ;; Canon
    "cap" "iiq" "eip" ;; Phase One
    "dcs" "dcr" "drf" "k25" "kdc" ;; Kodak
    "dng" ;; Adobe, Leica
    "erf" ;; Epson
    "fff" ;; Imacon/Hasselblad raw
    "gif" 
    "ico"
    "jpeg" "jpg" 
    "m4v"
    "mef" ;; Mamiya
    "mdc" ;; Minolta, Agfa
    "mos" ;; Leaf
    #_"mov"
    #_"mp4"
    "mrw" ;; Minolta, Konica Minolta
    "nef" "nrw" ;; Nikon
    "orf" ;; Olympus
    "pcx"
    "pef" "ptx" ;; Pentax
    "png" 
    "psd"
    "pxn" ;; Logitech
    "r3d" ;; RED Digital Cinema
    "raf" ;; Fuji
    "raw" ;; Panasonic, Leica
    "rw2" ;; Panasonic 
    "rwl" ;; Leica
    "rwz" ;; Rawzor
    "srw" ;; Samsung
    "tif" "tiff"
    "webp"
    "x3f" ;; Sigma
    })
;;----------------------------------------------------------------
(defn image-file? [^File f] (image-file-type? (file-type f)))
;;----------------------------------------------------------------
(defn image-file-seq [^File d]
  (assert (.exists d))
  (filter image-file? (file-seq d)))
;;----------------------------------------------------------------
(def ^:private arw-format 
  (DateTimeFormatter/ofPattern "yyyy:MM:dd HH:mm:ss"))
(defn- parse-datetime ^LocalDateTime [^String s]
  (LocalDateTime/parse s arw-format))
;;----------------------------------------------------------------
(defn exif-datetime ^LocalDateTime [^File f exif]
  (if-not exif
    nil
    (let [dts (get exif "Date/Time")]
      (if-not dts 
        (do 
          (println "no Date/Time:" (upathname f))
          (pp/pprint exif)
          #_(print-exif f))
        (parse-datetime dts)))))
;;----------------------------------------------------------------
(defn file-attributes ^Map [^File f]
  (Files/readAttributes
    (.toPath f) 
    "*" 
    ^"[Ljava.nio.file.LinkOption;" (make-array LinkOption 0)))
;;----------------------------------------------------------------
(defn- filetime-to-localdatetime ^LocalDateTime [^FileTime ft]
  (LocalDateTime/ofInstant 
    (.toInstant ft) 
    ZoneOffset/UTC))
;;----------------------------------------------------------------
(defn image-file-datetime ^LocalDateTime [^File f]
  (try
    (let [exif (exif/exif-for-file f)
          ldt (if (empty? exif)
                (println "no exif:" (upathname f))
                (exif-datetime f exif))]
      (if (nil? ldt)
        (let [attributes (file-attributes f)
              filetime (or (.get attributes "lastModifiedTime")
                           (.get attributes "creationTime"))]
          (filetime-to-localdatetime filetime))
        ldt))
    (catch Throwable t
      (println "error:" (upathname f))
      (binding [*err* *out*] (stacktrace/print-cause-trace t))
      (throw t))))
;;----------------------------------------------------------------
(defn- file-checksum [^File file]
  (let [input (FileInputStream. file)
        digest (MessageDigest/getInstance "MD5")
        stream (DigestInputStream. input digest)
        bufsize (* 1024 1024)
        buf (byte-array bufsize)]
    (while (not= -1 (.read stream buf 0 bufsize)))
    (apply str (map (partial format "%02x") (.digest digest)))))
;;----------------------------------------------------------------
(defn- same-image [^File f0 ^File f1]
  )
;;----------------------------------------------------------------
