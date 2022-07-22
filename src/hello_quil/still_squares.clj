(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def black [0 0 0])
(def white [255 255 255])
(def green [131 152 77])

(def bg-color black)
(def fg-color green)

(defn clear-screen []
  (apply q/background bg-color))

(def step-size 50)
(def num-steps 20)

(defn setup []
  (clear-screen)
  (apply q/stroke fg-color)
  (q/no-fill)
  (q/no-loop) ;; useful when we just want to call draw once to draw a single still image
)

(defn draw []
  (let [big-square (* step-size num-steps)]
    (q/with-translation [(/ (- (q/width)  big-square) 2)
                         (/ (- (q/height) big-square) 2)]
      (doseq [num (range num-steps)]
        (let [step (* step-size (inc num))]
          (q/rect 0 0 step step))))))

(q/defsketch demo
  :title "title goes here"
  :size :fullscreen ; [300 300]
  :setup setup
  :draw draw)
