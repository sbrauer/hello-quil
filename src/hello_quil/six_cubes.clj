(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn setup []
  ;;(q/background 0) ; black
  ;; Set frame rate to 30 frames per second.
  (q/frame-rate 60)
  ;; Set color mode to HSB (HSV) instead of default RGB.
  ;;(q/color-mode :hsb)
  ;;(q/fill 0)
  (q/text-size 20)
  ;; setup function returns initial state

  (q/fill 255)

  {:bouncer {:n 0
             :inc? true
             :max-val 400}
   :eyeX (rand 50)
   :eyeY (rand 50)
   :eyeZ (rand 50)})

(defn next-bounce
  [{:keys [n inc? max-val]}]
  (let [next-n (if inc? (inc n) (dec n))]
    {:n next-n
     :inc? (if inc?
             (< next-n max-val)
             (<= next-n 0))
     :max-val max-val}))

(defn update-state [{:keys [bouncer eyeX eyeY eyeZ] :as state}]
  {:bouncer (next-bounce bouncer)
   :eyeX (mod (+ 2 eyeX) (/ (q/width) 2))
   :eyeY (mod (inc eyeY) (/ (q/height) 2))
   :eyeZ (mod (inc eyeZ) (/ (q/width) 2))})

(defn draw-state [{:keys [bouncer eyeX eyeY eyeZ] :as state}]
  ;;(q/background 200)

  ;;(q/fill 0)
  ;;(q/text (str state) 20 20)

  (q/camera eyeX eyeY eyeZ 0 0 0 0 0 -1)

  (let [shape q/box ;; q/sphere
        {:keys [n]} bouncer
        m (+ 50 n)]
    (q/with-translation [m 0 0]
      (shape n))
    (q/with-translation [(- m) 0 0]
      (shape n))
    (q/with-translation [0 m 0]
      (shape n))
    (q/with-translation [0 (- m) 0]
      (shape n))
    (q/with-translation [0 0 m]
      (shape n))
    (q/with-translation [0 0 (- m)]
      (shape n))))

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
