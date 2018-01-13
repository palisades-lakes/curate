;;----------------------------------------------------------------
;; exif-processor has reflection and boxing warnings
(set! *warn-on-reflection* false)
(set! *unchecked-math* false)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.curate
  
  {:doc "photo curation utilities"
   :author "palisades dot lakes at gmail dot com"
   :version "2018-01-12"}
  
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
;; image files
;;----------------------------------------------------------------
(defn upathname 
  "return a unix style pathname string."
  ^String [^File f]
  (s/replace (.getPath f) "\\" "/"))
;;----------------------------------------------------------------
(defn file-prefix ^String [^File f]
  (let [filename (.getName f)
        i (s/last-index-of filename ".")]
    (if (nil? i)
      filename
      (s/lower-case (subs filename 0 i)))))
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
    #_"gif" 
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
;; image metadata
;;----------------------------------------------------------------
(defn print-image-metadata [^File f]
  (let [^Metadata m 
        (try (ImageMetadataReader/readMetadata f)
          (catch Throwable t
            (binding [*err* *out*]
              (stacktrace/print-cause-trace t))
            (throw t)))
        ^Iterable ds (.getDirectories m)
        exif (exif/exif-for-file f)]
    (doseq [^Directory d (seq ds)]
      #_(println)
      #_(println ".....................................")
      (println "..." (class d) (.getName d) (.getTagCount d))
      (doseq [^Tag tag (.getTags d)]
        (println 
          (.getTagName tag) ":" (.getDescription tag)
          (str "[" (.getDirectoryName tag) " " 
               (.getTagTypeHex tag) "]"))
        
        #_(println (.toString tag))))
    #_(let [^ExifSubIFDDirectory d 
            (.getFirstDirectoryOfType m ExifSubIFDDirectory)
            date 
            (.getDate d ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
        (println date))
    (pp/pprint exif)))
;;----------------------------------------------------------------
;; datetimes
;;----------------------------------------------------------------
(def ^:private datetime-regex #"((?i)date)|((?i)time)")
(defn- dt-string? [^String s] (re-find datetime-regex s))
;;----------------------------------------------------------------
(defn exif-map-datetimes ^Map [^Map exif]
  (into (sorted-map) (filter #(dt-string? (key %))) exif))
;;----------------------------------------------------------------
(defn print-exif-map-datetimes [^Map exif]
  (pp/pprint (exif-map-datetimes exif)))
;;----------------------------------------------------------------
(defn- directory-has-datetimes? [^Directory d]
  (loop [tags (seq (.getTags d))]
    (if (empty? tags)
      false
      (let [^Tag tag (first tags)]
        (if (dt-string? (.getTagName tag))
          true
          (recur (rest tags)))))))
;;----------------------------------------------------------------
(defn print-image-metadata-datetimes [^File f]
  (let [^Metadata m 
        (try (ImageMetadataReader/readMetadata f)
          (catch Throwable t
            (binding [*err* *out*]
              (stacktrace/print-cause-trace t))
            (throw t)))
        ^Iterable ds (.getDirectories m)
        exif (exif-map-datetimes (exif/exif-for-file f))]
    (doseq [^Directory d (seq ds)]
      (when (directory-has-datetimes? d)
        #_(println "----------------------------------------")
        (println (class d) (.getName d) (.getTagCount d))
        (doseq [^Tag tag (.getTags d)]
          (when (dt-string? (.getTagName tag))
            (println (.getTagTypeHex tag) (.getTagName tag) 
                     ":" (.getDescription tag))
            (println (.toString tag)))))
      #_(let [^ExifSubIFDDirectory d 
              (.getFirstDirectoryOfType m ExifSubIFDDirectory)
              date 
              (.getDate d ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
          (println date)))
    (when-not (empty? exif) (pp/pprint exif))))
;;----------------------------------------------------------------
(def ^:private arw-format 
  (DateTimeFormatter/ofPattern "yyyy:MM:dd HH:mm:ss"))
(defn- parse-datetime ^LocalDateTime [^String s]
  (LocalDateTime/parse s arw-format))
(def ^:private file-prefix-format 
  (DateTimeFormatter/ofPattern "yyyyMMdd-HHmmss"))
(def ^:private year-format 
  (DateTimeFormatter/ofPattern "yyyy"))
(def ^:private month-format 
  (DateTimeFormatter/ofPattern "MM"))
;;----------------------------------------------------------------
(defn exif-datetime ^LocalDateTime [^File f ^Map exif]
  (if-not exif
    nil
    (let [dts (get exif "Date/Time")]
      (if dts 
        (parse-datetime dts)
        #_(do 
            (println "no Date/Time:" (upathname f))
            (print-image-metadata-datetimes f)
            (println))))))
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
;; camera make/model
;;----------------------------------------------------------------
(defn image-file-make ^String [^File f]
  (try
    (let [exif (exif/exif-for-file f)
          make (if (empty? exif)
                 (println "no exif:" (upathname f))
                 (get exif "Make"))
          ^String make (when make (s/lower-case make))
          make (if (and make (.startsWith make "nikon"))
                 "nikon"
                 make)]
      (when (nil? make)
        (println)
        (println 
          "-----------------------------------------------------")
        (println (upathname f))
        (print-image-metadata f))
      make)
    (catch Throwable t
      (println "error:" (upathname f))
      (binding [*err* *out*] (stacktrace/print-cause-trace t))
      (throw t))))
;;----------------------------------------------------------------
(defn image-file-model ^String [^File f]
  (try
    (let [exif (exif/exif-for-file f)
          model (if (empty? exif)
                  (println "no exif:" (upathname f))
                  (get exif "Model"))
          ^String model (when model (s/lower-case model))
          ^String model (when model (s/replace model #"[ \-]+" ""))]
      (when (nil? model)
        (println)
        (println 
          "-----------------------------------------------------")
        (println (upathname f))
        (print-image-metadata f))
      model)
    (catch Throwable t
      (println "error:" (upathname f))
      (binding [*err* *out*] (stacktrace/print-cause-trace t))
      (throw t))))
;;----------------------------------------------------------------
(defn image-file-camera ^String [^File f]
  (try
    (let [make (image-file-make f)
          model (image-file-model f)
          model (when (and model make) (s/replace model make ""))]
      (when (nil? model)
        (println)
        (println 
          "-----------------------------------------------------")
        (println (upathname f))
        (print-image-metadata f))
      (str make model))
    (catch Throwable t
      (println "error:" (upathname f))
      (binding [*err* *out*] (stacktrace/print-cause-trace t))
      (throw t))))
;;----------------------------------------------------------------
;; renaming
;;----------------------------------------------------------------
(defn new-path ^File [^File f ^File new-folder]
  (try
    (let [^LocalDateTime ldt (image-file-datetime f)
          ^String year (format "%04d" (.getYear ldt))
          ^String month (format "%02d" (.getMonthValue ldt))
          ^String prefix (.format ldt file-prefix-format)
          ^String ext (file-type f)
          ^String suffix (or (image-file-camera f)
                             (file-prefix f))
          ^File new-path (io/file 
                           new-folder year month 
                           (str prefix "-" suffix "." ext))]
      #_(io/make-parents new-path)
      new-path)
    (catch Throwable t
      (println "error:" (upathname f))
      (binding [*err* *out*] (stacktrace/print-cause-trace t))
      (throw t))))
;;----------------------------------------------------------------
;; file equality
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
