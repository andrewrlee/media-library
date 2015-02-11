(ns media-library.file
  (require  [clojure.string :as s]
            [clojure.pprint :use pprint]
            [clojure.java.io :refer [file]])
  (:gen-class))

(defn- get-suffix     [file-name] (second (re-find #"^.*(\..*?)$" file-name)))
(defn- without-suffix [file-name] (or  (second (re-find #"^(.*?)(-\d+)*\..*?$" file-name)) file-name))

(defn- has-suffix? [file-name suffixes] 
  (let [suffixes-as-regexp (map #(re-pattern (str ".*\\" %)) suffixes)]  
    (some #(re-find % file-name) suffixes-as-regexp)))

(defn- exclude-non-movies [file-seq] 
  (filter (fn [file]  (and (not (.isDirectory file)) 
                           (not (has-suffix? (.getName file) [".srt" ".db" ".sub"]))) ) file-seq) )

(defn- movie->map [base-path]
  (fn [movie]  (let [name                 (.getName movie)
                     path                 (.getPath movie)
                     normalized-base-path (s/replace base-path #"/$" "")]
                 {:name (without-suffix  name)
                  :type (get-suffix name)
                  :path (s/replace-first (.getPath movie) normalized-base-path "")})))

(defn movie-seq [base-path]   
  (->> (file-seq (java.io.File. base-path "Video/Movies") )
       (exclude-non-movies)
       (sort-by #(s/lower-case (.getName %)))
       (map (movie->map base-path))))

(defn- add-series [base-path]
  (letfn [(map-folder-contents [f folder] (vec (map f (sort-by #(.getName %) (.listFiles folder))))) 
          (episodes-for-series [series-folder] 
            [(.getName series-folder) (map-folder-contents (movie->map base-path) series-folder)])]
    (fn  [[show-folder show]]
      (assoc show :series (map-folder-contents episodes-for-series show-folder)))))

(defn series-seq [base-path]
  (let [root-dir          (file base-path "Video/Tv" )
        shows             (.listFiles root-dir)
        file-and-data-seq (map (juxt identity (movie->map base-path)) shows)]
    (map (add-series base-path) file-and-data-seq) ))

(defn print-duplicates [movies] 
  (let [duplicates (filter #(< 1 (count (val %))) (group-by :name movies))]
    (doseq [movie duplicates] (println (key movie) (map :path (val movie))))))

(defn pprint->string [m]
  (let [w (java.io.StringWriter.)] (pprint m w)(.toString w)))

(defn read-edn-file  [file]      
  (read-string (slurp file)))

(defn write-edn-file [file data] 
  (println "Dumping file:" file) 
  (spit file (pprint->string data)))
