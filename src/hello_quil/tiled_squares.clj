(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.colors :as colors]))

(def black [0 0 0])

;; pick a palette
(def colors colors/beach)

(defn clear-screen []
  (apply q/background black))

(def state-atom (atom {:size 100 :num-colors 4}))

(defn setup []
  (q/no-stroke)
  (q/no-loop))

(defn draw []
  (clear-screen)
  (let [{:keys [size num-colors]} @state-atom
        win-w (q/width)
        win-h (q/height)
        cols (int (/ win-w size))
        rows (int (/ win-h size))
        colors (take num-colors (shuffle colors))]
    (q/with-translation [[(/ (- win-w (* cols size)) 2)
                          (/ (- win-h (* rows size)) 2)]]
      (doseq [r (range rows)]
        (doseq [c (range cols)]
          (let [bg (rand-nth colors)]
            (q/fill bg)
            (q/rect (* c size)
                    (* r size)
                    size
                    size)))))))

(defn key-pressed []
  (when-let [k (q/key-as-keyword)]
    ;;(prinln "hey-pressed " k)
    (case k
      :left (swap! state-atom update :size #(max 1 (dec %)))
      :right (swap! state-atom update :size inc)
      :down (swap! state-atom update :num-colors #(max 2 (dec %)))
      :up (swap! state-atom update :num-colors #(min (count colors) (inc %)))
      nil)
    (q/redraw)))

(q/defsketch demo
  :title "title goes here"
  :size :fullscreen
  ;; :size [640 480]
  :features [:resizable]
  :setup setup
  :draw draw
  :key-pressed key-pressed)
