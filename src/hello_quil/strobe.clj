(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]))

(def state-atom (atom nil))

(def white [255 255 255])
(def black [0 0 0])
(def default-colors [white black])

(defn setup []
  (apply q/background black)
  (q/no-stroke)
  (reset! state-atom {:rate 20
                      :colors default-colors
                      :color-idx 0})
  (q/frame-rate (:rate @state-atom)))

(defn update-state! []
  (swap! state-atom update :color-idx #(mod (inc %) (count (:colors @state-atom)))))

(defn draw []
  (let [{:keys [colors color-idx rate]} @state-atom]
    (apply q/background (nth colors color-idx))
    (update-state!)))

(defn add-rand-color! [state-atom]
  (let [{:keys [colors color-idx]} @state-atom  ]
    (swap! state-atom merge {:colors [(nth colors color-idx) (vec (repeatedly 3 #(int (rand 256))))]
                             :color-idx 0})))

(defn reset-colors! [state-atom]
  (swap! state-atom assoc :colors default-colors))

(defn key-pressed []
  (when-let [k (q/key-as-keyword)]
    ;;(prinln "key-pressed " k)
    (case k
      :left (swap! state-atom update :rate #(max 1 (dec %)))
      :right (swap! state-atom update :rate inc)
      :space (add-rand-color! state-atom)
      :r (reset-colors! state-atom)
      ;; default expression to avoid "No matching clause" exception.
      nil)
    (q/frame-rate (:rate @state-atom))
    (prn @state-atom)))

(q/defsketch hello-quil
  :title "Hello World"
  :size :fullscreen
  ;;:size [1000 1000]
  :setup setup
  :draw draw
  :features [:keep-on-top]
  :renderer :p3d
  :key-pressed key-pressed)
