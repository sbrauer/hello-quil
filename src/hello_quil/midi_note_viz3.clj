(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.midi-util :as midi]))

;; Ideally should be configurable instead of hard-coded
(def midi-device-description "IAC Driver IAC Bus 1")

(def state-atom (atom {:midi-dev nil
                       :notes {}}))

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

(defn note-key->color
  [note]
  (nth diatonic-colors (mod note 12)))

(defn setup-midi!
  [dev-description handler-fn]
  (let [dev (or (:midi-dev @state-atom)
                (midi/input-device dev-description))]
    (midi/clear-handlers! dev)
    (midi/add-handler! dev handler-fn)
    (swap! state-atom assoc :midi-dev dev)))

(defn release-midi!
  []
  (when-let [dev (:midi-dev @state-atom)]
    (midi/clear-handlers! dev)))

(defn draw-note
  [{:keys [channel key velocity] :as note}]
  ;;(println "draw-note" note)
  (q/fill (note-key->color key))
  (let [h (q/height)
        w (/ (q/width) 128)
        y (- (q/height) h)
        x (* key w)]
    (q/rect x y w h)))

(defn clear-screen []
    (q/background 0))

(defn draw []
  ;;(println "DRAW" @state-atom)
  (let [{:keys [notes]} @state-atom]
    (clear-screen)
    (doseq [note (vals notes)]
      (draw-note note))))

(defn handle-midi-event
  [event-info _timestamp]
  (let [{:keys [command channel key velocity]} event-info]
    ;;(println command event-info)
    (case command
      :on  (swap! state-atom update :notes assoc key event-info)
      :off (swap! state-atom update :notes dissoc key)
      :default)))

(defn setup
  []
  (setup-midi! midi-device-description handle-midi-event)
  (clear-screen)
  (q/frame-rate 16)
  (q/no-stroke))

(q/defsketch demo
  :title "midi quil fun"
  :setup setup
  :draw draw
  :on-close release-midi!

  ;;:size [640 480]
  :size :fullscreen
  ;;:features [:keep-on-top]
)
