(ns kinetic-fun.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [org.httpkit.server :as kit]))

(defn ws_handler [request]
  (kit/with-channel request channel
    (kit/on-close channel (fn [status] (println "channel closed: " status)))
    (kit/on-receive channel (fn [data]
                              (println data)
                              (kit/send! channel data)))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/ws" [] ws_handler)
  (route/resources "/")
  (route/not-found "Not Found"))

(defn -main [& args]
  (kit/run-server (handler/site app-routes) {:port 3000}))
