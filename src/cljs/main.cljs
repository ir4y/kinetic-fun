(ns kinetic.main
    (:use [cljs.core :only [js-obj]]
          [jayq.core :only [$ css]])
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


(.on circle "dragmove" (fn []
                         (let [mouse_pos (.getMousePosition stage)
                               x (.-x mouse_pos)
                               y (.-y mouse_pos)
                               pos (str "(x=" x ";y=" y ")")]
                               (when (and (not (= x last-x)) (not (= y last-y)))
                                     (set! last-x x)
                                     (set! last-y y)
                                     (.send conn pos)
                                     (.draw layer)))))


(.add layer background)
(.add layer log)
(.add layer circle)
(.add stage layer)

(when (.-MozWebSocket js/window)
  (set! (.-WebSocket js/window) (.-MozWebSocket js/window)))


(defn open-conenction []
  (set! conn (js/WebSocket. "ws://localhost:3000/ws"))
  (set! (.-onmessage conn) (fn [event] 
                             (.setText log (.-data event))
                             (.draw layer))))
(open-conenction)
