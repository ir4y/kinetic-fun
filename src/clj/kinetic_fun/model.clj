(ns kinetic-fun.model
    (:require [taoensso.carmine :as car]
              [org.httpkit.server :as kit]))


(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec))

(defmacro wcar [& body] `(car/with-conn pool spec-server1 ~@body))

(defn publish-circle [message]
  (wcar (car/publish "circle" message)))

(defn setup-listener [channel]
  (car/with-new-pubsub-listener 
    spec-server1 {"circle" (fn f1 [msg] 
                             (let [json (get msg 2)]
                               (when (not (= json 1))
                                 (kit/send! channel json))))}
    (car/subscribe "circle")))
