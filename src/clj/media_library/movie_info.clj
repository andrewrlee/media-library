(ns media-library.movie-info
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.set    :refer [rename-keys]])
  (:gen-class))

(declare save-poster retrieve-details confirm-choice)

(defn- decorate [media-type] 
  (fn [item]
    (let [url-prefix      (str (name media-type) "/"  (:id item))
          item-with-links (assoc item :links 
                            {:keywords    (str url-prefix "/keywords")
                             :images      (str url-prefix "/images")
                             :videos      (str url-prefix "/videos") 
                             :details     (str url-prefix)
                             :poster_path (:poster_path item)} )
          tv-keys         (fn [item] (rename-keys item {:name :title, :first_air_date :release_date, :episode_run_time :runtime}))]
       (tv-keys item-with-links))))

(defn- find-media [client media-type term] 
  (map (decorate media-type) (:results (.json-request client (str "search/" (name media-type)) {:query term}))))

(defn- get-rel [client movie rel] (.json-request client (get-in movie [:links rel])))

(defn- get-keywords [client movie] (vec (:keywords (get-rel client movie :keywords))))

(defn- format-details [movie keywords] 
  (let [details (select-keys movie [:title :release_date :series :overview :imdb_id :runtime :genres :spoken_languages :links])
        details-with-kw (assoc details :keywords keywords)
        names-only (fn [v] (vec (map :name v)))]
    (reduce (fn [acc kw] (update-in acc [kw] names-only)) details-with-kw [:genres :spoken_languages :keywords])))

(defn- get-details [client media-type movie] 
  (let [movie    ((decorate media-type) (get-rel client movie :details))
        keywords (get-keywords client movie)] 
    (format-details movie keywords))) 

(defn- print-movie [index {:keys [title release_date] :as m}]
  (println index ": " title "(" release_date ")"))

(defn- confirm-choice [client media-type selected-index term movie]
  (let [movie-details (get-details client media-type movie)]
    (print "## Selected: ") 
    (print-movie selected-index movie-details)
    (pprint movie-details)
    (println "## Ok? Yes(y) No(n) Ignore(i)")
    (let [input (read-line)]
      (cond (= "i" input) nil
            (= "n" input) (retrieve-details client media-type term)
            (= "y" input) movie-details))))

(defn retrieve-details [client media-type term]
  (let [matches (vec  (find-media client media-type term))
        range   (range 0 (count matches))]
    (cond
      (= 1 (count matches))
      (confirm-choice client media-type 0 term (first matches)) 
      (seq matches)
        (do (doall (map print-movie range matches))
            (println "Select number that matches this item or no match(i)") 
            (let [input          (read-line)
                  selected-index (Integer. (or (re-find #"^\d+$" input) -1))
                  chosen         (get matches (Integer. selected-index))]
              (cond (= "i" input)
                      nil
                    (not (contains? (set range) selected-index))
                      (do (println "## Out of range! try again!")
                          (retrieve-details client media-type term))
                    :else
                    (confirm-choice client media-type selected-index term chosen)))))))

(defn save-poster [client output-file poster-link]
  (let [image (.image-request client poster-link)]
    (with-open [w (io/output-stream output-file)]
      (.write w image))))
