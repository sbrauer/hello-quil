(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]))

(def state-atom (atom nil))

(def size-step 40)
(def min-size 20)
(def spin-step 0.01)
(def min-spin 0.01)

(defn setup []
  (q/background 0)                      ; black
  (q/frame-rate 20)
  (q/stroke 255)                        ; white
  (q/stroke-weight 10)
  (q/fill 0)
  (reset! state-atom
          {:size 200
           :spin min-spin
           :x-angle 0.0
           :y-angle 0.0
           :z-angle 0.0
           :x-locked? false
           :y-locked? false
           :z-locked? false
           :translation-vec [(/ (q/width)  2)
                             (/ (q/height) 2)]}))

(defn update-angle [spin angle]
  (mod (+ angle spin) (* 2 q/PI)))

(defn draw []
  (q/background 0)
  (let [{:keys [size spin x-angle y-angle z-angle translation-vec x-locked? y-locked? z-locked?]} @state-atom
        angle-fn (partial update-angle spin)]
    (q/with-translation translation-vec
      (q/rotate-x x-angle)
      (q/rotate-y y-angle)
      (q/rotate-z z-angle)
      (q/box size))
    (when-not x-locked?
      (swap! state-atom update :x-angle angle-fn))
    (when-not y-locked?
      (swap! state-atom update :y-angle angle-fn))
    (when-not z-locked?
      (swap! state-atom update :z-angle angle-fn))))

(defn key-pressed []
  (when-let [k (q/key-as-keyword)]
    ;;(prinln "hey-pressed " k)
    (case k
      :x (swap! state-atom update :x-locked? not)
      :y (swap! state-atom update :y-locked? not)
      :z (swap! state-atom update :z-locked? not)
      :left (swap! state-atom update :size #(max min-size (- % size-step)))
      :right (swap! state-atom update :size #(+ % size-step))
      :down (swap! state-atom update :spin #(max min-spin (- % spin-step)))
      :up (swap! state-atom update :spin #(+ % spin-step))
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
