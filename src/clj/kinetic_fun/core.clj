(ns kinetic-fun.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.util.middleware :as noir]
            [noir.session :as session]
            [noir.cookies :as cookies]
            [ring.util.response :as resp]
            [org.httpkit.server :as kit]
            [cheshire.core :as json]
            [kinetic-fun.model :as model]))

(defn setup-new-coords [coords]
  (let [data (json/parse-string coords)]
    (model/publish-circle coords)
    (session/put! :circle-x (data "x"))
    (session/put! :circle-y (data "y"))))


(defn ws_handler [request]
  (session/put! :value "ws")
  (println (session/get! :value))
  (kit/with-channel request channel
    (kit/on-close channel (fn [status]                            
                            (println "channel closed: " status)))
    (kit/on-receive channel setup-new-coords)
    (def listener (model/setup-listener channel))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/push" [value] (println (cookies/get :ring-session)) (session/put! :value value) "pushed")
  (GET "/pop" [] (str "pop:" (session/get :value)))
  (GET "/ws" [] ws_handler)
  (route/resources "/")
  (route/not-found "Not Found"))

(defn -main [& args]
  (kit/run-server  (session/wrap-noir-session
                     (cookies/wrap-noir-cookies
                       (handler/site app-routes)) {:store model/redis-session-store}) {:port 3000}))
