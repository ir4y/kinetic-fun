(ns kinetic-fun.model
    (:require [taoensso.carmine :as car]))


(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec))

(defmacro wcar [& body] `(car/with-conn pool spec-server1 ~@body))

(def listener
    (car/with-new-pubsub-listener
         spec-server1 {"foobar" (fn f1 [msg] (println "Channel match: " msg))
                                        "foo*"   (fn f2 [msg] (println "Pattern match: " msg))}
         (car/subscribe  "foobar" "foobaz")
         (car/psubscribe "foo*")))

(wcar (car/publish "foobar" "Hello to foobar!"))
