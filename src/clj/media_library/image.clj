(ns media-library.image
  (:require [clojure.java.io :refer [file]]
            [image-resizer.resize :refer [resize-fn]]
            [image-resizer.scale-methods :refer [ultra-quality]])
  (:import  [javax.imageio ImageIO])
  (:gen-class))

(defn resize-to-file [output-dir f [width height]]
  (let [name (.getName f)
        output-file (file output-dir name)] 
    (if (not (.exists output-file))  
      (do (println "resizing " name "...")
          (ImageIO/write ((resize-fn width height ultra-quality) f) 
                         "jpg" output-file)))))

(defn resize-images [image-dir output-dir dimensions]
  (let [images (filter #(not (.isDirectory %)) (file-seq (file image-dir)))]  
    (doseq [image images] 
      (resize-to-file output-dir image dimensions))))

(defn -main [& args] (resize-images "output/images" "output/resized" [166 236]))


