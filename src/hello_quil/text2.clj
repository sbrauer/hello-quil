(ns text-example.core
  (:require [quil.core :as q]))

(def blue     [ 53 108 237])
(def yellow   [235 229  20])
(def white    [255 255 255])
(def grey     [200 200 200])
(def darkgrey [ 64  64  64])

(defn setup
  []
  (q/smooth)
  (q/frame-rate 20)
  (q/stroke-weight 0)
  (apply q/background blue)
  (def state (atom {:flipflop true})))

(defn draw
  []
  (let [{:keys [bg fg]} (if (:flipflop @state)
                          {:bg blue :fg white}
                          {:bg yellow :fg blue})]
    (apply q/background bg)
    (apply q/fill fg)

    (q/text-font (q/create-font "VCR OSD Mono" 32 true))
    (q/text-align :center :center)

    (q/text "This is not a test.\nDo not adjust your TV set.\nAwait further instructions.\nRemain calm." 0 0 (q/width) (q/height))

    (q/rect  10 200 100 100)
    (q/rect 130 200 100 100)
    (q/rect 250 200 100 100)
    (q/rect 370 200 100 100)

    (apply q/fill bg)

    (q/text "1"  50 260)
    (q/text "2" 170 260)
    (q/text "3" 290 260)
    (q/text "4" 410 260)))

(defn key-pressed []
  (swap! state update :flipflop not))

(q/sketch :title "Text Example"
          :setup setup
          :draw  draw
          :size  :fullscreen
          :features [:present]
          :key-pressed key-pressed)
