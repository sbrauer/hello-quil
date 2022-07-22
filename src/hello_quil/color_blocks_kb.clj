(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def state-atom (atom {:frame-rate  2
                       :palette-idx 0}))

(def palettes
  [
   ;; "raisin" grays
   [[ 64  57  70]
    [112 107 116]
    [159 156 163]
    [207 206 209]]

   [[255 126 116]  ;; orange
    [255 229 150]  ;; yellow
    [129 215 109]  ;; green
    [ 87 172 249]] ;; blue

   [[ 27  40  83]  ;; dark blue
    [ 19  71 125]  ;; blue
    [  4 196 202]  ;; light blue
    [243 206 117]] ;; yellow

   [[131 152 77]   ;; green
    [240 181 31]   ;; gold
    [234 112 11]   ;; orange
    [ 51 107 139]] ;; blue

   [[155  58  51]  ;; rust
    [241  87  59]  ;; orange
    [248 162  53]  ;; gold
    [254 251 232]] ;; cream

   ;; retro van from https://logosbynick.com/70s-color-palettes-with-hex/
   [[ 53  65  38]
    [243 184 123]
    [187 108  93]
    [ 53  41  33]]

   ])

(defn setup []
  (q/background 0) ; black
  (q/no-stroke)
  (q/frame-rate (:frame-rate @state-atom)))

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
        x (nth [0 w w 0] num)]
    (q/rect x y w h)))

(defn draw-triangle
  [num]
  (let [h (q/height)
        w (q/width)
        mid-x (/ w 2)
        mid-y (/ h 2)]
    (case num
      0 (q/triangle mid-x mid-y 0 0 w 0)
      1 (q/triangle mid-x mid-y w 0 w h)
      2 (q/triangle mid-x mid-y 0 h w h)
      3 (q/triangle mid-x mid-y 0 0 0 h))))

(defn clear-screen [_]
  (q/background 0))

(def shapes
  [(juxt clear-screen draw-vertical-bar)
   draw-vertical-bar
   (juxt clear-screen draw-quadrant)
   draw-quadrant
   (juxt clear-screen draw-horizontal-bar)
   draw-horizontal-bar
   (juxt clear-screen draw-triangle)
   draw-triangle

   (juxt clear-screen draw-vertical-bar)
   (juxt clear-screen draw-quadrant)
   (juxt clear-screen draw-horizontal-bar)
   (juxt clear-screen draw-triangle)
   draw-vertical-bar
   draw-quadrant
   draw-horizontal-bar
   draw-triangle])

(defn draw []
  ;;(prn (str "frame-count:" (q/frame-count)))
  (let [frame-idx (dec (q/frame-count))
        num (mod frame-idx 4)
        shape-idx (mod (int (/ frame-idx 4)) (count shapes))
        colors (nth palettes (:palette-idx @state-atom))]

    (when (zero? num)
      (clear-screen nil))

    (q/fill (nth colors num))

    ((nth shapes shape-idx) num)))

(defn key-pressed []
  (let [k (q/key-as-keyword)]
    (prn "hey-pressed " k)
    (case k
      :space (swap! state-atom update :palette-idx #(mod (inc %) (count palettes)))
      :+ (swap! state-atom update :frame-rate inc)
      :- (swap! state-atom update :frame-rate #(max 1 (dec %)))
      ;; default expression to avoid "No matching clause" exception.
      nil)
    (prn @state-atom)
    (q/frame-rate (:frame-rate @state-atom))))

(q/defsketch demo
  :title "boxes!"
  :size :fullscreen ; [300 300]
  :setup setup
  :draw draw
  :key-pressed key-pressed)
