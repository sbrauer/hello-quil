(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(def state-atom (atom {:frame-rate 20}))

(def black 0)
(def white 255)

(def num-steps 50)

(defn setup []
  ;;(q/no-loop)
  (q/no-fill)
  (q/stroke-weight 10)
  (q/frame-rate (:frame-rate @state-atom)))

(defn draw-step
  [num]
  (q/background (if (zero? (mod num 2)) black white))
  (q/stroke (if (zero? (mod num 2)) white black))
  ;;(q/fill (if (zero? (mod num 2)) white black))
  (let [{:keys [center-x center-y]} @state-atom
        lsize (* 2 (max (q/width) (q/height)))]

    (q/with-translation [center-x center-y]
      ;; Draw lines
      (doseq [ang (range 0 360 10)]
        (let [rang (q/radians (+ ang num))]
          (q/line 0
                  0
                  (* (q/cos rang) lsize)
                  (* (q/sin rang) lsize))))
      ;; Draw circles
      (doseq [r (range 0 num)]
        (let [csize (* 100 r)]
          (q/ellipse 0 0 csize csize))))))

(defn draw []
  (let [frame-idx (dec (q/frame-count))
        num (mod frame-idx num-steps)]
    (when (zero? num)
      (swap! state-atom
             merge
             {:center-x (q/random (q/width))
              :center-y (q/random (q/height))}))
    (draw-step num)))

;; FIXME: Maybe support a key (like space) to reset/clear and pick new random center
(defn key-pressed []
  (let [k (q/key-as-keyword)]
    (prn "key-pressed " k)
    (case k
      :+ (swap! state-atom update :frame-rate inc)
      :- (swap! state-atom update :frame-rate #(max 1 (dec %)))
      :default)
    (prn @state-atom)
    (q/frame-rate (:frame-rate @state-atom))))

(q/defsketch demo
  :title "title goes here"
  :size :fullscreen ; [300 300]
  :features [:keep-on-top]
  :renderer :p3d
  :setup setup
  :draw draw
  :key-pressed key-pressed)
