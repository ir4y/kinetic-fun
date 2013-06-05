(ns kinetic-fun.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [org.httpkit.server :as kit]
            [cheshire.core :as json]))

(defn ws_handler [request]
  (kit/with-channel request channel
    (kit/on-close channel (fn [status] (println "channel closed: " status)))
    (kit/on-receive channel (fn [coords]
                              (println coords)
                              (let [data (json/parse-string coords)]
                                (println (data "x") (data "y"))
                                (kit/send! channel (json/generate-string data)))))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/ws" [] ws_handler)
  (route/resources "/")
  (route/not-found "Not Found"))

(defn -main [& args]
  (kit/run-server (handler/site app-routes) {:port 3000}))

;(json-str {:x 1})
