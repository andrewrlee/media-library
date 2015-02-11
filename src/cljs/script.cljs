(ns game 
  (:require [goog.events :as events]
            [goog.dom :as dom]
            [goog.ui.Dialog :as di]
            [clojure.string :as s]
            [ajax.core :refer [GET]]
            [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :refer [html]]))

(defn get-chosen-option [event option-list]
  (get option-list (.-selectedIndex (.-target event))))

(defn get-field [m field] (get-in m [:details field]))

(defn new-element [type] (.createElement js/document type))

(defn sort-by-field [movies field] (sort-by (fn [[_ m]] (get-field m field)) movies))

(defn populate-select [select-element values]
  (set! (.-innerHTML select-element) (html (for [v values] [:option v]))))

(defn build-select-html [id values]
  [:select {:id id} (for [v values] [:option v])])

(defn render-movie-details [file-path {:keys [details] :as item}]
  (let [{:keys [title overview genres runtime duration keywords]} details
        {series   :series} item
        duration  (str (if (vector? runtime) (first runtime) runtime))
        content   [:div 
                   [:div.detail-line [:span.label "Path"    ] [:span.value file-path                            ]]
                   [:div.detail-line [:span.label "Duration"] [:span.value duration                             ]]
                   [:div.detail-line [:span.label "Genres"  ] [:span.value (s/join ", " genres)                 ]]
                   [:div.detail-line [:span.label "Keywords"] [:span.value (str (s/join ", " keywords) "&nbsp;")]]
                   (if series
                     [:div
                       [:div.detail-line [:span.label "Series"]  (build-select-html "series-select" (map first series))]
                       [:div#episode-list] [:p overview]]
                     [:p overview])]]
    (html content)))

(defn render-series [[series-name  episodes]] 
  (let [episode-list (dom/getElement "episode-list")]
    (dom/removeChildren episode-list)
    (let [episodes-html (for [episode episodes] 
                              [:div.episode 
                                [:span.episode (:name  episode)] 
                                [:button {:name "play" :class "play-button" :data-path (:path episode)} "Play >>"]])]
      (set! (.-innerHTML episode-list) (html episodes-html))
      (doseq [button (array-seq (dom/getElementsByClass "play-button"))]
        (events/listen button "click" #(GET (str "/play" (.getAttribute button "data-path"))))))))

(defn show-series-details [series]
  (let [element (dom/getElement "series-select")]
    (render-series (first series))
    (events/listen element "change" (fn [event] (render-series (get-chosen-option event series))))))

(defn show-film-details [dialog file-path]
  (let [button-set            (goog.ui.Dialog.ButtonSet.)]
    (.addButton    button-set (clj->js  {:key "play" :caption "Play >>"}))
    (.setButtonSet dialog     button-set)
    (events/listen dialog "dialogselect" #(GET (str "/play" file-path)))))

(defn show-detail [element [file-path {:keys [details] :as item}]] 
  (fn [event]
    (let [dialog                       (goog.ui.Dialog.)
          button-set                   (goog.ui.Dialog.ButtonSet.)
          {:keys [title release_date]} details]
      (.setContent   dialog (render-movie-details file-path item))
      (.setTitle     dialog (str  title "  (" release_date ")"))
      (.setVisible   dialog true)
      (.setDraggable dialog false)
      (events/listen dialog "afterhide" #(.dispose dialog))
      (if (:series item)
        (show-series-details (:series item))
        (show-film-details   dialog file-path)))))
   
(defn render-movie [[file-path {:keys [details]} :as item]] 
  (let [element        (new-element "DIV")
        img            (new-element "IMG")
        path           (get-in details [:links :poster_path]) 
        {title :title} details]
    (if path
      (do (.add (.-classList element ) "movie")
          (.appendChild element img)
          (set! (.-src img) (str "/images" path))
          (.setAttribute  img "data-title" title)
          (events/listen element "click" (show-detail img item))
          element))))
 
(defn render-movie-panel [movies]
  (let [movie-panel (dom/getElement "movie-panel")
        item-count  (dom/getElement "item-count")]
    (dom/removeChildren movie-panel)
    (set! (.-innerHTML item-count)  (count movies))
    (doseq [i (sort-by-field movies :title)]   
      (if-let [child   (render-movie i)]
        (.appendChild movie-panel child)))))

(defn render-genre-bar [movies]
  (let [genres        (set (reduce (fn [acc [_  m]] (concat acc (get-field m :genres))) #{} movies))
        sorted-genres (vec (cons "All" (sort genres)))
        genre-select   (dom/getElement "genre-list")]
    (populate-select genre-select sorted-genres)
    (events/listen genre-select "change"
      (fn [event]
        (let [selected (get-chosen-option event sorted-genres)
              filtered-movies (filter (fn [[_ m]] (contains? (set (get-field m :genres)) selected) )  movies)]
             (render-movie-panel (if (= selected "All") movies filtered-movies)))))))

(defn search [movies term]
  (let [pattern       (re-pattern (str "(?i)" term))
        matching      (fn [[_ m]] (if-let [title (get-field m :title)] (re-find pattern title) false))]
    (render-movie-panel (filter matching movies))))

(defn add-search-field-behaviour [movies]
  (let [field  (dom/getElement "search-field")]
    (events/listen field "keyup" (fn [e] 
      (set! (.-selectedIndex (dom/getElement "genre-list")) 0)
      (search movies (.-value field))))))

(defn load-data [response] 
  (let [data (cljs.reader/read-string response)]
    (render-movie-panel data)
    (render-genre-bar data)
    (add-search-field-behaviour data))) 

(GET "/data" {:handler load-data})
