(ns media-library.core
  (:require [media-library.build :as build]
            [media-library.serve :as serve])
  (:gen-class))

(defn -main [& args] 
  (if  (= (first args) "build")
    (apply build/-main args)
    (apply serve/-main args)))
