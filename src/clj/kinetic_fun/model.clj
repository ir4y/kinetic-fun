(ns kinetic-fun.model
    (:require [taoensso.carmine :as car]
              [org.httpkit.server :as kit]
              [clj-redis-session.core :as redis-session-core]))


(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec))

(defmacro wcar [& body] `(car/with-conn pool spec-server1 ~@body))

(defn get-all-balls []
  (for [session_id (wcar (car/keys "session:*"))]
    (let [{x "x" y "y"}  ((read-string (wcar (car/get session_id))) :noir)]
      {:session_id session_id :x x :y y})))

(def redis-session-store (redis-session-core/redis-store pool spec-server1))
