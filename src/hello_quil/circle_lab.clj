(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def black [0 0 0])
(def green [131 152 77])
(def purple [106 12 82])

(defn clear-screen []
  (apply q/background black))

(defn set-fg-color
  [color]
  (apply q/stroke color))

(defn setup []
  (clear-screen)
  (q/no-fill)
  (q/stroke-weight 2)
  (q/frame-rate 20))

(defn draw-grid-of-circles
  [color grid-size rows-cols]
  (set-fg-color color)
  (let [cell-size (/ grid-size rows-cols)
        circle-size (* 0.8 cell-size)]
    (doseq [row (range rows-cols)]
      (doseq [col (range rows-cols)]
        (let [x (* cell-size (+ 0.5 row))
              y (* cell-size (+ 0.5 col))]
          (q/ellipse x y circle-size circle-size))))))

(defn draw []
  (clear-screen)
  (let [fc (q/frame-count)
        rows-cols (inc (mod (int (/ fc 64)) 8))
        grid-size (* (q/height) 0.75)]
    (q/with-translation [(/ (- (q/width) grid-size) 2)
                         (/ (- (q/height) grid-size) 2)]
      (let [offset 10
            ang (mod fc 45)
            rang (q/radians (* 8 ang))]
        (q/with-translation [(* (q/cos rang) offset)
                             (* (q/sin rang) offset)]
          (draw-grid-of-circles purple grid-size rows-cols)))
      (draw-grid-of-circles green grid-size rows-cols))))

(q/defsketch demo
  :title "title goes here"
  :size :fullscreen
  ;;:size [640 480]
  :features [:resizable]
  :setup setup
  :draw draw)
