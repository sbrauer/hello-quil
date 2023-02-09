(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def state-atom (atom {:frame-rate 30}))

(def black [0 0 0])
(def white [255 255 255])
(def green [131 152 77])

(def bg-color black)
(def fg-color green)

(def num-steps 100)

(defn clear-screen []
  (apply q/background bg-color))

(defn setup []
  (clear-screen)
  (apply q/stroke fg-color)
  (q/no-fill)
  (q/frame-rate (:frame-rate @state-atom)))

(defn draw-step
  [num]
  (let [{:keys [center-x center-y width height]} @state-atom]
    (q/ellipse center-x center-y (* width num) (* height num))))

(defn draw []
  ;;(prn (str "frame-count:" (q/frame-count)))
  (let [frame-idx (dec (q/frame-count))
        num (mod frame-idx num-steps)]

    (when (zero? num)
      (clear-screen)
      (let [w (q/random 10 180)
            h (- 200 w)]
        (swap! state-atom
               merge
               {:center-x (q/random (q/width))
                :center-y (q/random (q/height))
                ;; for random ellipses
                :width w
                :height h})))

    (draw-step num)))

;; FIXME: Maybe support a key (like space) to reset/clear and pick new random center
(defn key-pressed []
  (let [k (q/key-as-keyword)]
    (prn "key-pressed " k)
    (case k
      :+ (swap! state-atom update :frame-rate inc)
      :- (swap! state-atom update :frame-rate #(max 1 (dec %)))
      ;; default expression to avoid "No matching clause" exception.
      nil)
    (prn @state-atom)
    (q/frame-rate (:frame-rate @state-atom))))

(q/defsketch demo
  :title "title goes here"
  :size :fullscreen ; [300 300]
  :setup setup
  :draw draw
  :key-pressed key-pressed)
