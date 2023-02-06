(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn setup []
  (q/background 0)                      ; black
  (q/frame-rate 20)
  (q/stroke 255)                        ; white
  (q/stroke-weight 5)
  (q/fill 0)
  (let [size-factor 0.15]
    {:size (* size-factor (q/width))
     :ang 0.0
     :translation-vec [(/ (q/width)  2)
                       (/ (q/height) 2)]}))

(defn update-state [state]
  (update state :ang (fn [ang] (mod (+ ang 0.1) (* 2 q/PI)))))

(defn draw-state [{:keys [size ang translation-vec] :as state}]
  (q/background 0)
  (q/with-translation translation-vec
      (q/rotate-x ang)
      (q/rotate-y ang)
      (q/rotate-z ang)
      (q/box size)))

(q/defsketch hello-quil
  :title "Hello World"
  :size :fullscreen
  ;;:size [1000 1000]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  :renderer :p3d
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
