(ns kinetic-fun.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [org.httpkit.server :as kit]
            [cheshire.core :as json]
            [kinetic-fun.model :as model]))

(defn ws_handler [request]
  (kit/with-channel request channel
    (kit/on-close channel (fn [status] (println "channel closed: " status)))
    (kit/on-receive channel (fn [coords]
                              (let [data (json/parse-string coords)]
                                (model/publish-circle coords))))
    (def listener (model/setup-listener channel))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/ws" [] ws_handler)
  (route/resources "/")
  (route/not-found "Not Found"))

(defn -main [& args]
  (kit/run-server (handler/site app-routes) {:port 3000}))
