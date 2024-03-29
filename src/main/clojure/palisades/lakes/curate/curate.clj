(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.curate.curate

  {:doc     "photo curation utilities"
   :author  "palisades dot lakes at gmail dot com"
   :version "2023-08-06"}

  (:refer-clojure :exclude [replace])
  (:require #_[clojure.set :as set]
    [clojure.string :as s]
    [clojure.pprint :as pp]
    [clojure.java.io :as io]
    [clojure.stacktrace :as stacktrace])
  (:import [clojure.lang IFn]
           [java.io File FileInputStream]
    #_[java.nio.file Files LinkOption]
    #_[java.nio.file.attribute FileTime]
           [java.security DigestInputStream MessageDigest]
           [java.time LocalDate LocalDateTime ZoneOffset]
           [java.time.format DateTimeFormatter]
           [java.util Arrays Collections LinkedHashMap Map]
           [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata Directory Metadata Tag]
    #_[com.drew.metadata.exif
       ExifIFD0Directory ExifSubIFDDirectory]))

;; TODO: force exif keys to lower case, other standardization
;; TODO: detect and label DXO processed files,
;; other image software
;;----------------------------------------------------------------
;; string utilities
;;----------------------------------------------------------------
(defn- replace ^String [^String s match ^String replacement]
  "null safe."
  (when (and s match)
    (s/replace s match replacement)))
(defn- starts-with? [^String s ^String prefix]
  "null safe."
  (when (and s prefix) (.startsWith s prefix)))
(defn ends-with? [^String s ^String suffix]
  "null safe."
  (when (and s suffix) (.endsWith s suffix)))
(defn- lower-case ^String [^String s]
  "null safe."
  (when s (s/lower-case s)))
;;----------------------------------------------------------------
;; image files
;;----------------------------------------------------------------
(defn- unix-path
  "return a unix style pathname string."
  ^String [^File f]
  (when f (s/replace (.getPath f) "\\" "/")))
;;----------------------------------------------------------------
(defn- file-prefix ^String [^File f]
  (let [filename (.getName f)
        i (s/last-index-of filename ".")]
    (if (nil? i)
      filename
      (s/lower-case (subs filename 0 i)))))
;;----------------------------------------------------------------
(defn- file-type ^String [^File f]
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
    #{#_"3fr"                                               ;; Hasselblad
      "3g2" "3gp"
      #_"ari"                                               ;; Arri Alexa
      "arw" #_"srf" #_"sr2"                                 ;; Sony
      #_"bay"                                               ;; Casio
      "bmp"
      #_"cri"                                               ;; Cintel
      #_"crw" "cr2"                                         ;; Canon
      #_"cap" #_"iiq" #_"eip"                               ;; Phase One
      #_"dcs" #_"dcr" #_"drf" #_"k25" #_"kdc"               ;; Kodak
      "dng"                                                 ;; Adobe, Leica
      #_"erf"                                               ;; Epson
      #_"fff"                                               ;; Imacon/Hasselblad raw
      "gif"
      "ico"
      "jpeg" "jpg"
      "m4v"
      #_"mef"                                               ;; Mamiya
      #_"mdc"                                               ;; Minolta, Agfa
      #_"mos"                                               ;; Leaf
      "mov"
      "mp4"
      #_"mrw"                                               ;; Minolta, Konica Minolta
      "nef" #_"nrw"                                         ;; Nikon
      "orf"                                                 ;; Olympus
      "pcx"
      #_"pef" #_"ptx"                                       ;; Pentax
      "png"
      "psd"
      #_"pxn"                                               ;; Logitech
      #_"r3d"                                               ;; RED Digital Cinema
      "raf"                                                 ;; Fuji
      "raw"                                                 ;; Panasonic, Leica
      "rw2"                                                 ;; Panasonic
      "rwl"                                                 ;; Leica
      #_"rwz"                                               ;; Rawzor
      "srw"                                                 ;; Samsung
      "tif" "tiff"
      "webp"
      "x3f"                                                 ;; Sigma
      })
(def ^:private image-file-type?
  #{"3fr"                                                   ;; Hasselblad
    #_"3g2"
    #_"3gp"
    "ari"                                                   ;; Arri Alexa
    "arw" "srf" "sr2"                                       ;; Sony
    "bay"                                                   ;; Casio
    "bmp"
    "cri"                                                   ;; Cintel
    "crw" "cr2"                                             ;; Canon
    "cap" "iiq" "eip"                                       ;; Phase One
    "dcs" "dcr" "drf" "k25" "kdc"                           ;; Kodak
    "dng"                                                   ;; Adobe, Leica
    "erf"                                                   ;; Epson
    "fff"                                                   ;; Imacon/Hasselblad raw
    #_"gif"
    "ico"
    "jpeg" "jpg"
    "m4v"
    "mef"                                                   ;; Mamiya
    "mdc"                                                   ;; Minolta, Agfa
    "mos"                                                   ;; Leaf
    #_"mov"
    #_"mp4"
    "mrw"                                                   ;; Minolta, Konica Minolta
    "nef" "nrw"                                             ;; Nikon
    "orf"                                                   ;; Olympus
    "pcx"
    "pef" "ptx"                                             ;; Pentax
    "png"
    "psd"
    "pxn"                                                   ;; Logitech
    "r3d"                                                   ;; RED Digital Cinema
    "raf"                                                   ;; Fuji
    "raw"                                                   ;; Panasonic, Leica
    "rw2"                                                   ;; Panasonic
    "rwl"                                                   ;; Leica
    "rwz"                                                   ;; Rawzor
    "srw"                                                   ;; Samsung
    "tif" "tiff"
    "webp"
    "x3f"                                                   ;; Sigma
    })
;;----------------------------------------------------------------
(defn- image-file? [^File f]
  ;; filter out some odd hidden files in recycle bins, etc.
  (and (not (starts-with? (.getName f) "$"))
       (image-file-type? (file-type f))))
;;----------------------------------------------------------------
(defn image-file-seq

  "Return a <code>seq</code> of all the files, in any folder under
   <code>d</code>, that are accepted by
   <code>image-file?</code>., which at present is just a set of
   known image file endings."

  [^File d]

  (assert (.exists d) (.getPath d))
  (filter image-file? (file-seq d)))
;;----------------------------------------------------------------
;; image metadata
;;----------------------------------------------------------------
(defn- log-error [^Map exif ^File f ^Throwable t]
  (println "ERROR:" (unix-path f))
  (pp/pprint exif)
  (binding [*err* *out*] (stacktrace/print-cause-trace t))
  (throw t))
;;----------------------------------------------------------------
(defn- directory-map ^Map [^Directory d]
  (into {}
        (map (fn [^Tag t] [(.getTagName t) (.getDescription t)])
             (.getTags d))))
;;----------------------------------------------------------------
(defn- exif-maps

  "Return the image meta data as a map from <code>Directory</code>
   to a <code>Map</code> of tag-value pairs.
   I am ignoring the parent-child relation among exif directories,
   (for now), because metadata-extractor ignores it as well."

  ^Map [^File f]

  (let [^Metadata m (try (ImageMetadataReader/readMetadata f)
                         (catch Throwable t
                           (log-error nil f t)))
        ^Map lhm (LinkedHashMap.)]
    ;; preserve iteration order, so later looks can give
    ;; priority to first occurence of a tag.
    ;; ? might not be a good idea
    (doseq [^Directory d (.getDirectories m)]
      (.put lhm d (directory-map d)))
    (Collections/unmodifiableMap lhm)))
;;----------------------------------------------------------------
(defn print-exif [^File f]
  (pp/pprint (exif-maps f)))
;;----------------------------------------------------------------
(defn format-exif ^String [^File f]
  (with-out-str (print-exif f)))
;;----------------------------------------------------------------
;; datetimes
;;----------------------------------------------------------------
(def ^:private datetime-regex #"((?i)date)|((?i)time)")
(defn- dt-string? [^String s] (re-find datetime-regex s))
;;----------------------------------------------------------------
#_(defn exif-map-datetimes ^Map [^Map metadata]
    (into (sorted-map) (filter #(dt-string? (key %))) exif))
;;----------------------------------------------------------------
#_(defn print-exif-map-datetimes [^Map exif]
    (pp/pprint (exif-map-datetimes exif)))
;;----------------------------------------------------------------
#_(defn- directory-has-datetimes? [^Directory d]
    (loop [tags (seq (.getTags d))]
      (if (empty? tags)
        false
        (let [^Tag tag (first tags)]
          (if (dt-string? (.getTagName tag))
            true
            (recur (rest tags)))))))
;;----------------------------------------------------------------
#_(defn print-exif-datetimes [^File f]
    (let [^Metadata m
          (try (ImageMetadataReader/readMetadata f)
               (catch Throwable t
                 (binding [*err* *out*]
                   (stacktrace/print-cause-trace t))
                 (throw t)))
          ^Iterable ds (.getDirectories m)
          exif (exif-map-datetimes (exif-maps f))]
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
  (when s (LocalDateTime/parse s arw-format)))
(def ^:private file-prefix-format
  (DateTimeFormatter/ofPattern "yyyyMMdd-HHmmss"))
(def ^:private year-format
  (DateTimeFormatter/ofPattern "yyyy"))
(def ^:private month-format
  (DateTimeFormatter/ofPattern "MM"))
;;----------------------------------------------------------------
(defn- get-all ^Iterable [^Map exif ^String k]
  (keep #(get % k) (vals exif)))
(defn- get-first ^String [^Map exif ^String k]
  (first (get-all exif k)))
;;----------------------------------------------------------------
#_(defn file-attributes ^Map [^File f]
    (Files/readAttributes
      (.toPath f)
      "*"
      ^"[Ljava.nio.file.LinkOption;" (make-array LinkOption 0)))
;;----------------------------------------------------------------
#_(defn- filetime-to-localdatetime ^LocalDateTime [^FileTime ft]
    (LocalDateTime/ofInstant
      (.toInstant ft)
      ZoneOffset/UTC))
;;----------------------------------------------------------------
#_(defn- exif-datetime
    (^LocalDateTime [^Map exif]
     (when-not (empty? exif)
       (parse-datetime (get-first exif "Date/Time"))))
    (^LocalDateTime [^Map exif ^File f]
     (try
       (let [ldt (if (empty? exif)
                   (println "no exif:" (unix-path f))
                   (exif-datetime exif))]
         (if (nil? ldt)
           (let [attributes (file-attributes f)
                 filetime (or (.get attributes "creationTime")
                              (.get attributes "lastModifiedTime")
                              )]
             (filetime-to-localdatetime filetime))
           ldt))
       (catch Throwable t (log-error exif f t)))))
(defn- exif-datetime
  (^LocalDateTime [^Map exif ^File f]
   (try
     (when-not (empty? exif)
       (parse-datetime (get-first exif "Date/Time")))
     (catch Throwable t (log-error exif f t)))))
;;----------------------------------------------------------------
;; camera make/model
;;----------------------------------------------------------------
(defn- exif-width ^long [^Map exif ^File f]
  (try
    (let [s (or (get-first exif "Exif Image Width")
                (get-first exif "Image Width"))
          [n units] (s/split s #"\s")]
      (when units (assert (= units "pixels")))
      (Long/parseLong n))
    (catch Throwable t (log-error exif f t))))
;;----------------------------------------------------------------
(defn- exif-height ^long [^Map exif ^File f]
  (try
    (let [s (or (get-first exif "Exif Image Height")
                (get-first exif "Image Height"))
          [n units] (s/split s #"\s")]
      (when units (assert (= units "pixels")))
      (Long/parseLong n))
    (catch Throwable t (log-error exif f t))))
;;----------------------------------------------------------------
(defn exif-wxh
  (^String [^Map exif ^File f]
   (str (exif-width exif f) "x" (exif-height exif f)))
  (^String [^File f] (exif-wxh (exif-maps f) f)))
;;----------------------------------------------------------------
(defn- exif-make ^String [^Map exif ^File f]
  (try
    (let [;;exif (exif-maps f)
          make (replace (lower-case (get-first exif "Make"))
                        " " "")
          make (if (starts-with? make "nikon") "nikon" make)
          make (if (starts-with? make "pentax") "pentax" make)]
      #_(when (nil? make)
          (println)
          (println
            "-----------------------------------------------------")
          (println "no exif make:" (unix-path f))
          (pp/pprint exif))
      make)
    (catch Throwable t (log-error exif f t))))
;;----------------------------------------------------------------
(defn- exif-model ^String [^Map exif ^File f]
  (try
    (let [model (replace (lower-case (get-first exif "Model"))
                         " " "")
          ^String model (replace model #"[ \-]+" "")
          ^String model (replace model "*" "")]
      #_(when (nil? model)
          (println)
          (println
            "-----------------------------------------------------")
          (println "no exif model:" (unix-path f))
          (pp/pprint exif))
      model)
    (catch Throwable t (log-error exif f t))))
;;----------------------------------------------------------------
(defn exif-value
  (^String [^String k ^Map exif ^File f]
   (try
     (get-first exif k)
     (catch Throwable t (log-error exif f t))))
  (^String [^String k ^File f]
   (exif-value ^String k (exif-maps f) f)))
;;----------------------------------------------------------------
(defn exif-values
  (^String [^String k ^Map exif ^File f]
   (try
     (get-all exif k)
     (catch Throwable t (log-error exif f t))))
  (^String [^String k ^File f]
   (exif-values ^String k (exif-maps f) f)))
;;----------------------------------------------------------------
(defn exif-lens
  (^String [^Map exif ^File f]
   (let [lens (lower-case
                (or (exif-value "Lens Model" exif f)
                    (exif-value "Lens" exif f)
                    nil))
         lens (replace lens "\\" "")
         lens (replace lens "/" "")
         lens (replace lens " + " "-")
         lens (replace lens " " "")
         lens (replace lens "iphone6splus" "")
         lens (replace lens "camera" "")]
     lens))


  (^String [^File f] (exif-lens (exif-maps f) f)))
#_(defn exif-lens
    (^String [^Map exif ^File f]
     (exif-value "Lens" exif f))
    (^String [^File f] (exif-lens (exif-maps f) f)))
;;----------------------------------------------------------------
(defn exif-camera
  (^String [^Map exif ^File f]
   (try
     (let [^String make (exif-make exif f)
           ^String model (exif-model exif f)
           ^String make (if (starts-with? model "hp") "" make)
           ^String model (replace model make "")
           make-model (str make model)
           make-model (replace make-model "pentaxpentax" "pentax")]
       #_(when (empty? make-model)
           (println)
           (println
             "-----------------------------------------------------")
           (println "no exif make-model" (unix-path f))
           (pp/pprint exif))
       make-model)
     (catch Throwable t (log-error exif f t))))
  (^String [^File f] (exif-camera (exif-maps f) f)))
;;----------------------------------------------------------------
(defn exif-software
  (^String [^Map exif ^File f]
   (try (get-all exif "Software")
        (catch Throwable t (log-error exif f t))))
  (^String [^File f] (exif-software (exif-maps f) f)))
;;----------------------------------------------------------------
;; separate camera originals from processed images
;;  pairs : [keystring folder]
;; Search for match terminates with 1st success, so order matters
(def ^:private processors
  [["Adobe" "adobe"]
   ["DxO" "dxo"]
   ["Image Data" "sony"]
   ["Microsoft" "microsoft"]
   ["Nikon Transfer" "nikon"]
   ["Picasa" "google"]
   ["Photos 1.5" "apple"]
   ["PMB" "sony"]
   ["Roxio" "roxio"]
   ["Skitch" "skitch"]
   ["ViewNX" "nikon"]])
(defn exif-processor
  (^String [^Map exif ^File f]
   (try
     (let [^String software (first (exif-software exif f))
           processor (when-not (empty? software)
                       (first
                         (keep
                           (fn [[^String k ^String v]]
                             (when (.contains software k) v))
                           processors)))]
       (or processor "original"))
     (catch Throwable t (log-error exif f t))))
  (^String [^File f] (exif-processor (exif-maps f) f)))
;;----------------------------------------------------------------
;; file equality
;;----------------------------------------------------------------
#_(defn- file-checksum [^File file]
    (let [input (FileInputStream. file)
          digest (MessageDigest/getInstance "MD5")
          stream (DigestInputStream. input digest)
          nbytes (* 1024 1024)
          buf (byte-array nbytes)]
      (while (not= -1 (.read stream buf 0 nbytes)))
      (apply str (map (partial format "%02x") (.digest digest)))))
;;----------------------------------------------------------------
(defn- identical-contents? [^File f0 ^File f1]
  (let [i0 (FileInputStream. f0)
        i1 (FileInputStream. f1)
        n (* 1024 1024)
        ^bytes b0 (byte-array n)
        ^bytes b1 (byte-array n)]
    (loop []
      (let [n0 (.read i0 b0 0 n)
            n1 (.read i1 b1 0 n)]
        (cond
          (== -1 n0 n1) true
          (and (== n0 n1) (Arrays/equals b0 b1)) (recur)
          :else false)))))
;;----------------------------------------------------------------
;; renaming
;;----------------------------------------------------------------
(defn- new-filename
  (^File [^Map exif ^File f ^String version]
   (assert (not (nil? exif)))
   (assert (not (nil? f)))
   (try
     (let [^LocalDateTime ldt (exif-datetime exif f)
           ;^String year (if ldt
           ;               (format "%04d" (.getYear ldt))
           ;               "none")
           ;^String month (if ldt
           ;                (format "%02d" (.getMonthValue ldt))
           ;                "no")
           ^String prefix (if ldt
                            (.format ldt file-prefix-format)
                            (file-prefix f))
           ^String suffix (or (exif-camera exif f)
                              (file-prefix f))
           ^String wxh (exif-wxh exif f)
           ^String fname (if-not (empty? suffix)
                           (str prefix "-" suffix)
                           prefix)
           ^String fname (if (ends-with? fname (str "-" wxh))
                           fname
                           (str fname "-" wxh))
           ^String fname (if-not (empty? version)
                           (str fname "-" version)
                           fname)
           ^String processor (exif-processor exif f)
           ^String fname (if-not (.equals "original" processor)
                           (str fname "-" processor)
                           fname)]
       fname)
     (catch Throwable t (log-error exif f t))))
  (^File [^Map exif ^File f] (new-filename exif f nil)))
;;----------------------------------------------------------------
(defn- increment-version ^String [^String version]
  (if (empty? version)
    "1"
    (str (inc (Integer/parseInt version)))))
;;----------------------------------------------------------------
(defn- new-path-camera-lens-year-month-day
  (^File [^Map exif ^File f ^File d ^String version]
   (assert (not (nil? exif)))
   (assert (not (nil? f)))
   (try
     (let [^LocalDateTime ldt (exif-datetime exif f)
           ^String year (when ldt (format "%04d" (.getYear ldt)))
           ^String month (when ldt (format "%02d" (.getMonthValue ldt)))
           ^String day (when ldt (format "%02d" (.getDayOfMonth ldt)))
           ^String camera (exif-camera exif f)
           ^String lens (exif-lens exif f)
           ^String fname (new-filename exif f version)
           ^String ext (file-type f)
           ^File folder (apply
                          io/file
                          (remove nil? [d camera lens year month day]))
           ^File new-file (io/file folder (str fname "." ext))]
       new-file)
     (catch Throwable t (log-error exif f t))))
  (^File [^File f ^File d ^String version]
   (new-path-camera-lens-year-month-day (exif-maps f) f d version))
  (^File [^File f ^File d] (new-path-camera-lens-year-month-day f d nil)))
;;----------------------------------------------------------------
(defn- new-path-camera-lens-year-month
  (^File [^Map exif ^File f ^File d ^String version]
   (assert (not (nil? exif)))
   (assert (not (nil? f)))
   (try
     (let [^LocalDateTime ldt (exif-datetime exif f)
           ^String year (when ldt (format "%04d" (.getYear ldt)))
           ^String month (when ldt (format "%02d" (.getMonthValue ldt)))
           ^String camera (exif-camera exif f)
           ^String lens (exif-lens exif f)
           ^String fname (new-filename exif f version)
           ^String ext (file-type f)
           ^File folder (apply
                          io/file
                          (remove nil? [d camera lens year month]))
           ^File new-file (io/file folder (str fname "." ext))]
       new-file)
     (catch Throwable t (log-error exif f t))))
  (^File [^File f ^File d ^String version]
   (new-path-camera-lens-year-month (exif-maps f) f d version))
  (^File [^File f ^File d] (new-path-camera-lens-year-month f d nil)))
;;----------------------------------------------------------------
;; filters
;;----------------------------------------------------------------
;; return a closure that returns <code>true</code> for exif-datetime
;; strictly after the given date time.
(defn after-date? [^LocalDate inclusive]
  (fn [^Map exif ^File f]
    (when exif
      (let [^LocalDateTime ldt (exif-datetime exif f)]
        (and ldt (.isAfter (.toLocalDate ldt) inclusive))))))
;;----------------------------------------------------------------
;; TODO: parameterize folder pattern, ie, day vs no day. Pass in
;; function or keyword?
(defn rename-image
  ([^File f0 ^IFn tester ^File d echo-new? ^String version]
   ;(println)
   ;(println "tester"  tester)
   ;(println "f0"  f0)
   ;(println "d"  d)
   ;(println "echo" echo-new?)
   ;(println "version" version)
   ;(println)
   ;(flush)
   (try
     (let [^Map exif (exif-maps f0)]
       (if-not (.invoke tester exif f0)
         0
         (let [^File f1
               #_(new-path-camera-lens-year-month f0 d version)
               (new-path-camera-lens-year-month-day exif f0 d version)
               ]
           ;; no new path if image file not parsable
           (when f1
             (if-not (.exists f1)
               (do
                 (when echo-new?
                   (println "new:" (unix-path f0))
                   (println "--->" (unix-path f1)))
                 (io/make-parents f1)
                 (io/copy f0 f1)
                 1)
               (if (identical-contents? f0 f1)
                 0
                 (rename-image f0 tester d echo-new? (increment-version version))))))))
     (catch Throwable t (log-error (exif-maps f0) f0 t))))
  ([^File f0 ^IFn tester ^File d echo-new?] (rename-image f0 tester d echo-new? nil))
  #_([^File f0 ^IFn tester ^File d] (rename-image f0 tester d false nil))
  #_([^File f0 ^File d] (rename-image f0 (constantly true) d false nil)))
;;----------------------------------------------------------------
