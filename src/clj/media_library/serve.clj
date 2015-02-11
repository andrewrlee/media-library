(ns media-library.serve
  (:require [compojure.core :refer [defroutes routes GET]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as response]
            [media-library.file :as file]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.shell :refer [sh]])
  (:gen-class))

(defn play [{:keys [library-loc launch-command]} path] 
  (apply sh (conj launch-command (str library-loc path))))

(defn app-routes [config data] 
  (let [data-as-string (file/pprint->string data)]
    (routes 
     (GET "/" [] (response/resource-response "index.html" {:root "public"}))
     (GET "/data" [] data-as-string)
     (GET "/play/:path{.*?}" [path] (play config path))
     (route/files "/images/" {:root "images"})
     (route/resources "/")
     (route/not-found "Not Found"))))

(defn -main [& args] 
  (let [config (file/read-edn-file "config.edn")
        data   (file/read-edn-file "data.edn")]
    (println "config ->" config "data-items ->" (count data) "\nStarting Server..")
    (run-jetty 
     (handler/site (app-routes config data))
     {:port (:port config) :join? false})))
