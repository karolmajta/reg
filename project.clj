(defproject reg "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/clojurescript "1.8.40"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 [reagent "0.5.1"]]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-environ "1.0.2"]
            [lein-npm "0.6.2"]]
  :resource-paths ["dist/resources"]

  :profiles {:dev {:plugins [[lein-figwheel "0.5.2"]]}}
  :figwheel {}


  :npm {:root "dist"
        :dependencies [[electron-prebuilt "0.37.5"]
                       [source-map-support "0.4.0"]
                       [ws "1.0.1"]]
        :package {:main "main.js"
                  :scripts {:electron "./node_modules/.bin/electron ."}}}

  :cljsbuild {:builds {:server {:source-paths ["src/cljs"]
                                :compiler {:main reg.application.server
                                           :target :nodejs
                                           :optimizations :none
                                           :output-to "dist/cljsbuild-main.js"
                                           :output-dir "dist"
                                           :source-map true}}
                       :client {:source-paths ["src/cljs"]
                                :compiler {:main reg.application.client
                                           :optimizations :whitespace
                                           :output-to "dist/resources/js/main.js"
                                           :output-dir "dist/resources/js"
                                           :source-map "dist/resources/js/main.js.map"}}}})
