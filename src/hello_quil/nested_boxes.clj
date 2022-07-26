(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def black [0 0 0])
(def green [131 152 77])

(def bg-color black)
(def fg-color green)

(defn clear-screen []
  (apply q/background bg-color))

(def state-atom (atom {:w 50 :h 50 :num 10 :r 0}))

(defn setup []
  (clear-screen)
  (apply q/stroke fg-color)
  (q/no-fill)
  (q/frame-rate 10)
  ;;(q/no-loop) ;; useful when we just want to call draw once to draw a single still image
)

(defn draw []
  (clear-screen)
  (let [{:keys [w h num r]} @state-atom
        win-w (q/width)
        win-h (q/height)]
    (doseq [n (range num)]
      (let [n (inc n)
            width (* w n)
            height (* h n)]
        (q/rect (+ (/ (- win-w width) 2) (rand r))
                (+ (/ (- win-h height) 2) (rand r))
                width
                height)))))

(defn key-pressed []
  (when-let [k (q/key-as-keyword)]
    ;;(prinln "hey-pressed " k)
    (case k
      :left (swap! state-atom update :w #(max 1 (dec %)))
      :right (swap! state-atom update :w inc)
      :down (swap! state-atom update :h #(max 1 (dec %)))
      :up (swap! state-atom update :h inc)
      :a (swap! state-atom update :num #(max 1 (dec %)))
      :d (swap! state-atom update :num inc)
      :s (swap! state-atom update :r #(max 0 (dec %)))
      :w (swap! state-atom update :r inc)
      ;; default expression to avoid "No matching clause" exception.
      nil)
    (prn @state-atom)))

(q/defsketch demo
  :title "title goes here"
  :size :fullscreen
  ;;:size [640 480]
  :features [:resizable]
  :setup setup
  :draw draw
  :key-pressed key-pressed)
