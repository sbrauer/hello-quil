(ns text-example.core
  (:require [quil.core :as q]))

(def screen-width  640)
(def screen-height 480)

(def blue     [ 53 108 237])
(def yellow   [235 229  20])
(def white    [255 255 255])
(def grey     [200 200 200])
(def darkgrey [ 64  64  64])

(defn setup
  []
  (q/smooth)
  (q/frame-rate 20)
  ;; (q/text-font (q/create-font "VCR OSD Mono" 60 true))
  (apply q/background blue))

(defn draw
  []
  ;; `fill` sets the fill color (text color, and color inside shapes).
  (apply q/fill white)

  (q/text-font (q/create-font "VCR OSD Mono" 60 true))

  (let [x 50 y 100]
    (q/text "VIDEODROME" x y))

  (apply q/fill yellow)

  (let [x 50 y 200]
    (q/text "VIDEODROME" x y))

  (apply q/fill darkgrey)

  (let [x 55 y 305]
    (q/text "VIDEODROME" x y))

  (apply q/fill white)

  (let [x 50 y 300]
    (q/text "VIDEODROME" x y))

  (q/text-font (q/create-font "Helvetica Regular" 24 true))

  (let [x 50 y 360]
    (q/text "The quick brown fox jumps over the lazy dog.\nAll work and no play makes Jack a dull boy." x y))

  )

(q/sketch :title "Text Example"
          :setup setup
          :draw  draw
          :size  [screen-width
                  screen-height])
