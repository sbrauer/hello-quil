(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [uncomplicate.commons.core :as unc]
            [uncomplicate.clojure-sound.core :as cs]
            [uncomplicate.clojure-sound.midi :as midi]))

;; Ideally should be configurable instead of hard-coded
(def midi-device-description "IAC Driver IAC Bus 1")

(def state-atom (atom {:notes {}}))

(defn handle-midi-event
  [event _timestamp]
  (let [{:keys [command channel key velocity] :as info} (unc/info event)]
    (println command info)
    (case command
      :on  (swap! state-atom update :notes assoc key info)
      :off (swap! state-atom update :notes dissoc key)
      :default)))

(defn midi-input-device
  [dev-description]
  (->> (midi/device-info)
       (filter #(= dev-description  (unc/info % :description)))
       (map midi/device)
       (filter midi/transmitter?)
       first))

;; FIXME: manage device lifecycle; only have open at most once
(def midi-device (midi-input-device midi-device-description))
(cs/open! midi-device)
(cs/connect! midi-device (midi/receiver handle-midi-event))

;; https://colorswall.com/palette/3261
(def diatonic-colors
  [[226  48  88] ;; Amaranth (C)
   [247  88  58] ;; Orange Soda (C#/D♭)
   [248 148  62] ;; Royal Orange (D)
   [243 183  47] ;; Saffron (D#/E♭)
   [237 217  42] ;; Dandelion (E)
   [149 197  49] ;; Yellow-Green (F)
   [ 85 167  83] ;; Apple (F#/G♭)
   [ 17 130 110] ;; Deep Green-Cyan Turquoise (G)
   [ 49  97 163] ;; Lapis Lazuli (G#/A♭)
   [ 91  55 203] ;; Iris (A)
   [162  71 190] ;; Purple Plum (A#/B♭)
   [233  87 178] ;; Brilliant Rose (B)
   ])

;; converted from https://lospec.com/palette-list/atari-2600-palette-ntsc-version
(def note-colors
  [[0 0 0]
   [68 68 0]
   [112 40 0]
   [132 24 0]
   [136 0 0]
   [120 0 92]
   [72 0 120]
   [20 0 132]
   [0 0 136]
   [0 24 124]
   [0 44 92]
   [0 64 44]
   [0 60 0]
   [20 56 0]
   [44 48 0]
   [68 40 0]
   [64 64 64]
   [100 100 16]
   [132 68 20]
   [152 52 24]
   [156 32 32]
   [140 32 116]
   [96 32 144]
   [48 32 152]
   [28 32 156]
   [28 56 144]
   [28 76 120]
   [28 92 72]
   [32 92 32]
   [52 92 28]
   [76 80 28]
   [100 72 24]
   [108 108 108]
   [132 132 36]
   [152 92 40]
   [172 80 48]
   [176 60 60]
   [160 60 136]
   [120 60 164]
   [76 60 172]
   [56 64 176]
   [56 84 168]
   [56 104 144]
   [56 124 100]
   [64 124 64]
   [80 124 56]
   [104 112 52]
   [132 104 48]
   [144 144 144]
   [160 160 52]
   [172 120 60]
   [192 104 72]
   [192 88 88]
   [176 88 156]
   [140 88 184]
   [104 88 192]
   [80 92 192]
   [80 112 188]
   [80 132 172]
   [80 156 128]
   [92 156 92]
   [108 152 80]
   [132 140 76]
   [160 132 68]
   [176 176 176]
   [184 184 64]
   [188 140 76]
   [208 128 92]
   [208 112 112]
   [192 112 176]
   [160 112 204]
   [124 112 208]
   [104 116 208]
   [104 136 204]
   [104 156 192]
   [104 180 148]
   [116 180 116]
   [132 180 104]
   [156 168 100]
   [184 156 88]
   [200 200 200]
   [208 208 80]
   [204 160 92]
   [224 148 112]
   [224 136 136]
   [208 132 192]
   [180 132 220]
   [148 136 224]
   [124 140 224]
   [124 156 220]
   [124 180 212]
   [124 208 172]
   [140 208 140]
   [156 204 124]
   [180 192 120]
   [208 180 108]
   [220 220 220]
   [232 232 92]
   [220 180 104]
   [236 168 128]
   [236 160 160]
   [220 156 208]
   [196 156 236]
   [168 160 236]
   [144 164 236]
   [144 180 236]
   [144 204 232]
   [144 228 192]
   [164 228 164]
   [180 228 144]
   [204 212 136]
   [232 204 124]
   [236 236 236]
   [252 252 104]
   [252 188 148]
   [252 180 180]
   [236 176 224]
   [212 176 252]
   [188 180 252]
   [164 184 252]
   [164 200 252]
   [164 224 252]
   [164 252 212]
   [184 252 184]
   [200 252 164]
   [224 236 156]
   [252 224 140]
   [255 255 255]])

(defn note-key->color
  [note]
  ;;(nth diatonic-colors (mod note 12))
  (nth note-colors note)
  )

(defn setup []
  (q/background 0) ; black
  ;; what's the lowest that looks ok?
  ;;(q/frame-rate 32)
  (q/frame-rate 16)
  (q/no-stroke))

(defn draw-vertical-bar
  ;; num is 1-based (example: 1 of 3 total)
  [num total]
  (let [h (q/height)
        w (/ (q/width) total)
        y 0
        x (* (dec num) w)]
    (q/rect x y w h)))

(defn clear-screen []
  (q/background 0))

(defn draw []
  ;;(println "DRAW" @state-atom)
  (let [{:keys [notes]} @state-atom
        ks (sort (keys notes))
        total (count ks)]

    (clear-screen)

    (doseq [{:keys [num note]} (map-indexed (fn [i x] {:num (inc i) :note x}) ks)]
      (q/fill (note-key->color note))
      (draw-vertical-bar num total))))

;; FIXME: try adding an :on-close function so we can open the midi device in setup and close it in on-close.
(q/defsketch demo
  :title "midi quil fun"

  :size [640 480]

  ;;:size :fullscreen
  ;;:features [:keep-on-top]

  :setup setup
  :draw draw)
