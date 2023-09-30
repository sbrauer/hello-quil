(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.colors :as colors]))

(def black [0 0 0])
(def white [255 255 255])

(defn clear-screen []
  (apply q/background black))

(def state-atom (atom {:size 100}))

(defn setup []
  (q/no-stroke)
  (q/no-loop))

(defn draw []
;;   (clear-screen)
  (let [{:keys [size]} @state-atom
        win-w (q/width)
        win-h (q/height)
        cols (+ 2 (int (/ win-w size)))
        rows (+ 2 (int (/ win-h size)))]
    (q/with-translation [[(/ (- win-w (* cols size)) 2)
                          (/ (- win-h (* rows size)) 2)]]
      (doseq [r (range rows)]
        (doseq [c (range cols)]
          (let [i (+ (q/frame-count) r c)
                bg (if (even? i)
                     white
                     black)]
            (q/fill bg)
            (q/rect (* c size)
                    (* r size)
                    size
                    size)))))))

(defn key-pressed []
  (when-let [k (q/key-as-keyword)]
    ;;(prinln "key-pressed " k)
    (case k
      :left (swap! state-atom update :size #(max 1 (dec %)))
      :right (swap! state-atom update :size inc)
      :down (swap! state-atom update :size #(max 1 (- % 10)))
      :up (swap! state-atom update :size #(+ % 10))
      nil)
    (q/redraw)))

(q/defsketch demo
  :title "title goes here"

  :size :fullscreen
  :features [:present]

  ;; :size [640 480]
  ;; :features [:resizable]

  :setup setup
  :draw draw
  :key-pressed key-pressed)
