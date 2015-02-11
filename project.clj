(defproject media-library "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[clj-http "0.9.2"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [compojure "1.3.1"]
                 [org.clojure/clojure "1.6.0"]
                 [cheshire "5.3.1"]
                 [cljs-ajax "0.3.9"]
                 [hiccups "0.3.0"]
                 [environ "0.5.0"]
                 [image-resizer "0.1.6"]]
  :source-paths  ["src/clj"]         
  :plugins [[lein-environ "0.5.0"]]
  :main ^:skip-aot media-library.serve
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/clojurescript "0.0-2268"]]
                   :plugins [[lein-cljsbuild "1.0.4"]]}}
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/dist/movie.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]}
)
