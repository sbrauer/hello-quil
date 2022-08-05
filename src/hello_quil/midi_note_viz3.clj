(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.midi-util :as midi])
  (:import java.time.Instant))

;; Ideally should be configurable instead of hard-coded
(def midi-device-description "IAC Driver IAC Bus 1")

(def window-seconds 8)
(def window-microseconds (* 1000000 window-seconds))

;; The :notes map is keyed by midi key (0-128).
;; Each value is a map keyed by channel. Each value of that submap is a vector of note metadata maps, each with keys :vel, :on, and :off (maybe nil if we haven't received corresponding off yet).
;; Using a vector as there can be multiple overlapping notes with the same channel and key!
;; When a note off event comes in, we will try to match it up with the last
;; corresponding note (on same channel and key) with nil :off.
(def state-atom (atom {:midi-dev nil
                       :notes {}
                       :first-midi-ts-micros nil
                       :first-epoch-ts-millis nil}))

(defn current-epoch-millis []
  (inst-ms (Instant/now)))

; https://colorswall.com/palette/3261
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

(defn clear-screen []
    (q/background 0))

(defn draw-note
  ;; FIXME: x and w should be based on start-ts and stop-ts relative to current-timestamp and window-microseconds
  [{:keys [key start-ts stop-ts] :as note} current-timestamp]
  ;;(println "draw-note" note)
  (q/fill (note-key->color key))
  (let [h (/ (q/height) 128)
        w :FIXME-based-on-time
        y (* key h)
        x :FIXME-based-on-time]
    (q/rect x y w h)))

(defn draw []
  ;;(println "DRAW" @state-atom)
  (clear-screen)
  ;; FIXME
  (let [{:keys [notes first-epoch-ts-millis]} @state-atom
        end-millis (current-epoch-millis)
        start-millis (- end-millis (* window-seconds 1000))
        ]
    #_(doseq [[key note] notes]
        (draw-note (assoc note :key key) timestamp))))

(defn track-note-event
  [notes command velocity timestamp]
  (let [notes (or notes [])]
    (cond
      (= :on command) (conj notes {:on timestamp :vel velocity})
      (= :off command) (let [idx (first (keep-indexed (fn [idx item] (when-not (:off item) idx)) notes))]
                         (cond-> notes
                           idx (assoc-in [idx :off] timestamp)))
      :default notes)))

(defn handle-midi-event
  [event-info timestamp]
  ;; Note first timestamp of midi and system
  (when-not (:first-midi-ts-micros @state-atom)
    (swap! state-atom assoc
           :first-midi-ts-micros timestamp
           :first-epoch-ts-millis (current-epoch-millis)))

  (let [{:keys [command channel key velocity]} event-info]
    ;;;14M(println command event-info timestamp)
    (swap! state-atom assoc :midi-ts-micros timestamp)
    ;; FIXME: Also purge notes off older than window-seconds back from timestamp
    (when (#{:on :off} command)
      (swap! state-atom update-in [:notes key channel] #(track-note-event % command velocity timestamp)))))

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
