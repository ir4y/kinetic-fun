(ns kinetic.main
    (:use [cljs.core :only [js-obj]]))

(def stage
  (Kinetic/Stage. (js-obj "container" "container" "width" 578 "height" 200)))

(def layer
  (Kinetic/Layer.))

(def background
  (Kinetic/Rect. (js-obj "x" 0 "y" 0 "width" 578 "height" 200)))

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

(defn bigger [evt]
  (set! (.-cancelBubble evt) true)
  (.off circle "click.initial")
  (let [mouse_pos (.getMousePosition stage)
        click-x (.-x mouse_pos)
        click-y (.-y mouse_pos)]
    (.on layer "mousemove.bigger" (fn []
                                    (let [mouse_pos (.getMousePosition stage)
                                          x (.-x mouse_pos)
                                          y (.-y mouse_pos)
                                          distance (- click-x x)]
                                      (.setText log (str "(x=" x ";y=" y ")"))
                                      (when (> distance 0)
                                        (.setRadius circle distance)
                                        (.draw layer)))))
    (.on layer "click.bigger" (fn []
                                (.off layer "mousemove.bigger")
                                (.off layer "click.bigger")
                                (.on circle "click.initial" bigger)
                                ))))

(.on circle "click.initial" bigger)


(.add layer background)
(.add layer log)
(.add layer circle)
(.add stage layer)