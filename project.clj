(defproject kinetic-fun "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [jayq "2.3.0"]
                 [compojure "1.1.5"]
                 [com.taoensso/carmine "1.6.0"]
                 [lib-noir "0.6.0"]
                 [cheshire "5.0.2"]
                 [lib-noir "0.6.0"]
                 [clj-redis-session "1.0.0"]
                 [http-kit "2.1.2"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src/cljs"]
        :compiler {
          :output-to "resources/public/js/main.js"
          :optimizations :whitespace
          :pretty-print true}}]}
  :source-paths  ["src/clj" "src/cljs"]
  :main kinetic-fun.core)
