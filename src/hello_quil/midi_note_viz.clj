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
(def note-colors
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

(defn note-key->color
  [note]
  (nth note-colors (mod note 12)))

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
