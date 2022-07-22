(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def colors
  [[27 40 83]
   [19 71 125]
   [4 196 202]
   [243 206 117]])

(defn setup []
  (q/background 0) ; black
  (q/no-stroke)
  (q/frame-rate 2))

(defn draw-horizontal-bar
  [num]
  (let [w (q/width)
        h (/ (q/height) 4)
        y (* num h)
        x 0]
    (q/rect x y w h)))

(defn draw-vertical-bar
  [num]
  (let [h (q/height)
        w (/ (q/width) 4)
        y 0
        x (* num w)]
    (q/rect x y w h)))

(defn draw-quadrant
  [num]
  (let [h (/ (q/height) 2)
        w (/ (q/width) 2)
        y (if (< num 2) 0 h)
        x (if (zero? (mod num 2)) 0 w)]
    (q/rect x y w h)))

(defn clear-screen [_]
  (q/background 0))

(def shapes
  [(juxt clear-screen draw-vertical-bar)
   draw-vertical-bar
   (juxt clear-screen draw-quadrant)
   draw-quadrant
   (juxt clear-screen draw-horizontal-bar)
   draw-horizontal-bar

   (juxt clear-screen draw-vertical-bar)
   (juxt clear-screen draw-quadrant)
   (juxt clear-screen draw-horizontal-bar)
   draw-vertical-bar
   draw-quadrant
   draw-horizontal-bar])

(defn draw []
  (prn (str "frame-count:" (q/frame-count)))
  (let [frame-idx (dec (q/frame-count))
        num (mod frame-idx 4)
        shape-idx (mod (int (/ frame-idx 4)) (count shapes))]

    (when (zero? num)
      (q/background 0))

    (q/fill (nth colors num))

    ((nth shapes shape-idx) num)))

(q/defsketch demo
  :title "boxes!"
  :size :fullscreen ; [300 300]
  :setup setup
  :draw draw)
