(ns kinetic-fun.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.util.middleware :as noir]
            [noir.session :as session]
            [noir.cookies :as cookies]
            [ring.util.response :as resp]            
            [ring.middleware.session.store :as store]
            [ring.util.codec :as codec]
            [org.httpkit.server :as kit]
            [cheshire.core :as json]
            [kinetic-fun.model :as model]))

(defn setup-new-coords [coords]
  (let [data (json/parse-string coords)
        session_id (codec/url-decode (data "session"))
        session (store/read-session model/redis-session-store session_id)
        noir-session (merge (session :noir {}) data)]    
    (model/publish-circle coords)
    (store/write-session model/redis-session-store session_id (merge session {:noir noir-session}))))

(defn ws_handler [request]
  (kit/with-channel request channel
    (kit/on-close channel (fn [status]                            
                            (println "channel closed: " status)))
    (kit/on-receive channel setup-new-coords)
    (def listener (model/setup-listener channel))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/push" [value] (session/put! :value value) "pushed")
  (GET "/pop" [] (str "pop:" (session/get :value)))
  (GET "/ws" [] ws_handler)
  (GET "/init-data" [] (let [x (session/get "x" 100)
                             y (session/get "y" 100)] 
                         (json/generate-string {:x x :y y})))
  (GET "/all-balls" [] (json/generate-string (model/get-all-balls)))
  (route/resources "/")
  (route/not-found "Not Found"))

(defn -main [& args]
  (kit/run-server  (session/wrap-noir-session
                     (cookies/wrap-noir-cookies
                       (handler/site app-routes)) {:store model/redis-session-store}) {:port 3000}))
