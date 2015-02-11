(ns media-library.client
  (:require [cheshire.core :as chesh]
            [clj-http.client :as client]
            [clojure.string :as s])
  (:gen-class))

(def api-base-url "https://api.themoviedb.org/3/")
(def img-base-url "http://image.tmdb.org/t/p/w500")

(defn- contents-of 
  "Return a map of json retrieved from the specified url"
  [url] (chesh/parse-string (:body (client/get url)) true))

(defn- params->query-string [m]
  (letfn [(entry->string [[k v]] 
            (str (name k) "=" (java.net.URLEncoder/encode (str v))))]
    (s/join "&" (map entry->string m))))

(defn- make-json-request [key segment m] 
  (let [params       (assoc m :api_key  key)
        param-string (params->query-string params)
        url          (str api-base-url segment "?" param-string)]
    (contents-of url)))

(defn- make-image-request [segment]
  (:body (client/get (str img-base-url segment) {:as :byte-array})))

(defprotocol Client
  (json-request   [this segment]
                  [this segment params])
  (image-request  [this segment]))

(defrecord MovieDbClient [key]
 Client 
 (json-request  [_ segment] 
   (make-json-request key segment {})) 
 (json-request  [_ segment m] 
   (make-json-request key segment m))
 (image-request [_ segment]
   (make-image-request segment)) )
