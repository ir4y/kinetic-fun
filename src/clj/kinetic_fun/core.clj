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

(def my-channel (atom nil))

(defn setup-new-coords [session_id]
  (fn [coords]
    (let [data (json/parse-string coords)
          session (store/read-session model/redis-session-store session_id)
          noir-session (merge (session :noir {}) data)]    
      (reset! my-channel coords)
      (store/write-session model/redis-session-store session_id 
        (merge session {:noir noir-session})))))

(defn ws_handler [request]
  (let [session_id (cookies/get "ring-session")]
    (kit/with-channel request channel
      (kit/on-close channel (fn [status]                            
                              (remove-watch my-channel channel)))
      (kit/on-receive channel (setup-new-coords session_id))
      (add-watch my-channel channel
        (fn [_ _ _ json]
          (kit/send! channel json))))))

(defroutes app-routes
  (GET "/" [] 
       (session/put! "x" 100) 
       (session/put! "y" 100) 
       (resp/resource-response "index.html" {:root "public"}))
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
