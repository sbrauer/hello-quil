(ns hello-quil.spiral
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  (q/background 0) ; black
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:color 0
   :angle 0
   :n 0})

(defn update-state [{:keys [color angle n] :as state}]
  (let [new-n (+ n 0.4)
        max-n (/ (q/width) 1.6) ;; using a divisor a less than 2.0 so there's a delay before reset
        new-n (if (> new-n max-n) 0 new-n)]
    {:color (mod (+ color 0.7) 255)
     :angle (+ angle 0.1)
     :n new-n}))

(defn draw-state [{:keys [color angle n] :as state}]
  ; Clear the sketch
  (when (zero? n)
    (q/background 0))
  ; Set circle color.
  (q/fill color 255 255)
  ; Calculate x and y coordinates of the circle.
  (let [x (* n (q/cos angle))
        y (* n (q/sin angle))]
    ; Move origin point to the center of the sketch.
    (q/with-translation [(/ (q/width) 2)
                         (/ (q/height) 2)]
      ; Draw the circle.
      (q/ellipse x y 100 100)
      ;;(q/rect x y 100 100)
      )))


(q/defsketch hello-quil
  :title "You spin my circle right round"
  :size :fullscreen ;[500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  :renderer :p3d
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
