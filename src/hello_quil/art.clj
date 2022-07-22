(ns art
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  (q/frame-rate 30)                    ;; Set framerate in FPS
  (q/background 200))                 ;; Set the background colour to
                                      ;; a nice shade of grey.
(defn draw []
  (q/stroke (q/random 255)) ;; Set the stroke colour to a random grey
  (q/stroke-weight (q/random 10)) ;; Set the stroke thickness randomly
  (q/fill (q/random 255) (q/random 255) (q/random 255)) ;; Set the fill colour to a random grey

  #_(let [diam (q/random 200)
        x    (q/random (q/width))
        y    (q/random (q/height))]
    (q/ellipse x y diam diam))

  (let [h (+ 40 (q/random 200))
        w (+ 40 (q/random 200))
        x (q/random (q/width))
        y (q/random (q/height))]
    (q/rect x y w h)))

(q/defsketch example
  :title "Oh so many circles"    ;; Set the title of the sketch
  :settings #(q/smooth 2)             ;; Turn on anti-aliasing
  :setup setup                        ;; Specify the setup fn
  :draw draw                          ;; Specify the draw fn

  ;:size [640 480]
  ;:features [:keep-on-top]

  :size :fullscreen
  :features [:present :keep-on-top]

  ;;:middleware [m/fun-mode]
  )
