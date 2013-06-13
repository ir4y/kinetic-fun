(ns kinetic.main
    (:use [cljs.core :only [js-obj]]
          [jayq.core :only [$ css]])
    (:require
          [goog.net.cookies :as cookies])
    (:require-macros [jayq.macros :as jm]))



(def body-width 1280);(.width ($ :body)))

(def body-height 1024);(.height ($ :body)))

(def stage
  (Kinetic/Stage. (js-obj "container" "container" "width" body-width "height" body-height)))

(def layer
  (Kinetic/Layer.))

(def background
  (Kinetic/Rect. (js-obj "x" 0 "y" 0 "width" body-width "height" body-height)))

(def log
  (Kinetic/Text. (js-obj "fill" "red" "text" "No text yet")))

(def circle
  (Kinetic/Circle. (js-obj "x" (/ (.getWidth stage) 2)
                           "y" (/ (.getHeight stage) 2)
                           "radius" 70
                           "fill" "red"
                           "stroke" "black"
                           "strikeWidth" 4
                           "draggable" true)))

(def circles {})

(def session (cookies/get "ring-session"))

(.on circle "mouseover" (fn [] (this-as this
                                        (.setStroke this "blue")
                                        (.setStrokeWidth this 20)
                                        (.draw layer))))

(.on circle "mouseout" (fn [] (this-as this
                                        (.setStroke this "black")
                                        (.setStrokeWidth this 4)
                                        (.draw layer))))

(def conn (js-obj))
(def last-x 0)
(def last-y 0)


(.on circle "dragmove" (fn [] false
                         (let [mouse_pos (.getMousePosition stage)
                               x (.-x mouse_pos)
                               y (.-y mouse_pos)
                               coords (js-obj "x" x "y" y "session" session)
                               json-coords (.stringify js/JSON coords)]
                               (when (and (not (= x last-x)) (not (= y last-y)))
                                     (set! last-x x)
                                     (set! last-y y)
                                     (.send conn json-coords)
                                     (.draw layer)))))


(.add layer background)
(.add layer log)
(.add layer circle)
(.add stage layer)

(when (.-MozWebSocket js/window)
  (set! (.-WebSocket js/window) (.-MozWebSocket js/window)))

(defn open-conenction []
  (set! conn (js/WebSocket. "ws://192.168.0.102:3000/ws"))
  (set! (.-onmessage conn) (fn [event] 
                             (let [data (.-data event)
                                   data-json (.parse js/JSON data)
                                   session_id (js/decodeURIComponent (.-session data-json))
                                   used-circle (circles session_id)]
                               (when (not (= session_id (js/decodeURIComponent session)))
                                 (.setX used-circle (.-x data-json))
                                 (.setY used-circle (.-y data-json))
                                 (.draw layer))))))
(open-conenction)

(jm/let-ajax [initial {:url "/init-data" :dataType :json}
              all-balls {:url "/all-balls" :dataType :json}]
             (.setX circle (.-x initial))
             (.setY circle (.-y initial))
             (doall
               (for [ball all-balls]
                 (let [x (.-x ball)
                       y (.-y ball)
                       session_id (.-session_id ball)
                       new-circle  (Kinetic/Circle. (js-obj "x" x 
                                                            "y" y
                                                            "radius" 70
                                                            "fill" "blue"
                                                            "stroke" "black"
                                                            "strikeWidth" 4
                                                            "draggable" false))]
                   (when (not (= session_id (js/decodeURIComponent session)))
                     (set! circles (merge circles {session_id new-circle}))
                     (.add layer new-circle))))) 
             (.draw layer))


