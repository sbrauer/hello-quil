(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]))

(def state-atom (atom nil))

(def default-color [200 200 200])
(def black [0 0 0])

(defn setup []
  (apply q/background black)
  (q/frame-rate 60)
  (q/no-stroke)
  (let [w (q/width)
        h (q/height)]
    (reset! state-atom
            {:radius 1
             :max-radius (Math/sqrt (+ (* w w) (* h h)))
             :color-idx 0
             :colors [default-color black]
             :radius-step 10
             :translation-vec [(/ w  2) (/ h 2)]})))

(defn update-state! []
  (let [{:keys [radius max-radius colors color-idx radius-step]} @state-atom]
    (swap! state-atom update :radius #(+ % radius-step))
    (when (> radius max-radius)
      (swap! state-atom update :radius (constantly 1))
      (swap! state-atom update :color-idx #(mod (inc %) (count colors)))
      ;;(prn state-atom)
      )))

(defn draw []
  (let [{:keys [radius radius-step colors color-idx translation-vec]} @state-atom]
    (apply q/fill (nth colors color-idx))
    (apply q/background (nth colors (mod (inc color-idx) (count colors))))
    (q/with-translation translation-vec
      (q/ellipse 0 0 radius radius))
    (update-state!)))

(defn add-rand-color! [state-atom]
  (let [{:keys [colors color-idx]} @state-atom  ]
    (swap! state-atom merge {:colors [(nth colors color-idx) (vec (repeatedly 3 #(int (rand 256))))]
                             :color-idx 0})))

(defn key-pressed []
  (when-let [k (q/key-as-keyword)]
    ;;(prinln "key-pressed " k)
    (case k
      :left (swap! state-atom update :radius-step #(max 1 (- % 10)))
      :right (swap! state-atom update :radius-step #(+ % 10))
      :space (add-rand-color! state-atom)
      ;; default expression to avoid "No matching clause" exception.
      nil)
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
