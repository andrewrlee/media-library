(ns media-library.build
  (:require [media-library.movie-info :as info]
            [media-library.file :as file]
            [media-library.image :as image])
  (:use     [media-library.client :only (->MovieDbClient)])
  (:gen-class))

(defn- search-for-item [client type title] 
  (println "retrieving data for " title "...")
  (if-let [details (info/retrieve-details client type title)]
    {:details details}
    (do (println "No match for:" title ", ignore(i), quit(q) or search for different title(<new title>)")
        (let [input (read-line)]
          (cond (= "i" input) {:details nil}
                (= "q" input) nil 
                :else         (search-for-item client type input))))))

(defn- add-details [results item {:keys [details]}]
  (let [item-with-details (assoc item :details details)]
    (assoc results (:path item) item-with-details)))

(defn- items-still-to-process [results items] 
  (filter #((complement contains?) results (:path %)) items))

(defn- exclude-deleted-files [library-loc results]
  (letfn [(file-exists? [rel-path] (.exists (java.io.File. library-loc rel-path)))] 
    (select-keys results (for [[key _] results :when (file-exists? key)] key))))

(defn- generate-data-file [client type library-loc library-file items] 
  (let [results (exclude-deleted-files library-loc (file/read-edn-file library-file))]
    (if results (println "Skipping " (count results) " already processed items"))
    (loop [items     (items-still-to-process results items)
           results   results]
      (if (zero? (mod (count results) 20))
        (file/write-edn-file library-file results))
      (if-let [item (first items)]
        (if-let [details (search-for-item client type (:name item))]
          (recur (rest items) (add-details results item details))
          results)
        results))))

(defn- each-item [& fs] 
  (let [f (apply comp fs)]
    (fn [all-items] 
      (into {} (map (fn [[k v]] [k (if (nil? (:details v)) v (f v))]) all-items)))))

(defn- save-poster [client image-staging-dir] 
  (fn [item]
    (let [poster-link (get-in item [:details :links :poster_path])
          output-file (java.io.File. (str image-staging-dir poster-link))]
      (if (and poster-link (not (.exists output-file)))
        (do  (println "getting image for: " (get-in item [:details :title]) )
             (info/save-poster client output-file poster-link)))
    item)))

(defn- add-series-genre [item] 
  (if (and  (seq  (:series item)) 
            (nil? ((set (:genres item)) "Series")))
    (update-in item [:details :genres] (fn [genres] (set (conj genres "Series"))))
    (update-in item [:details :genres] (fn [genres] (set genres)))))

(defn build-library [seq-f type {:keys [library-loc] :as config} image-staging-dir library-file client]
  (println "config ->" config "\nStarting to build" type "library...")
  (->> (seq-f library-loc)
       (generate-data-file client type library-loc library-file)
       ((each-item 
         (save-poster client image-staging-dir)
         add-series-genre))
       (file/write-edn-file library-file))
   (image/resize-images image-staging-dir "images" [166 236]))

(defn -main [& args] 
  (let [config (file/read-edn-file "config.edn")
        client (->MovieDbClient (:movie-db-api-key config))]
    (build-library file/movie-seq  :movie config "images/temp" "data.edn" client)
    (build-library file/series-seq :tv    config "images/temp" "data.edn" client)))

