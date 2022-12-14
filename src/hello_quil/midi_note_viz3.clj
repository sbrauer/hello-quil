(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.midi-util :as midi])
  (:import java.time.Instant))

;; Ideally should be configurable instead of hard-coded
(def midi-device-description "IAC Driver IAC Bus 1")

;; Configure the window size in milliseconds
(def window-ms 8000)

;; The :notes map is keyed by midi key (0-128).
;; Each value is a map keyed by channel. Each value of that submap is a vector of note metadata maps, each with keys :vel, :on, and :off (maybe nil if we haven't received corresponding off yet).
;; Using a vector as there can be multiple overlapping notes with the same channel and key!
;; When a note off event comes in, we will try to match it up with the last
;; corresponding note (on same channel and key) with nil :off.
(def state-atom (atom {:midi-dev nil
                       :notes {}}))

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

(defn current-epoch-ms []
  (inst-ms (Instant/now)))

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
  [key on off start end]
  (q/fill (note-key->color key))
  (let [win-w (q/width)
        px-per-ms (/ win-w window-ms)
        h (/ (q/height) 128)
        y (* (- 128 key) h)
        x (if off
            (* px-per-ms (- end off))
            0)
        w (* px-per-ms
             (- (or off end) on))]
    (q/rect x y w h)))

(defn draw []
  ;;(println "DRAW" @state-atom)
  (clear-screen)
  (let [end (current-epoch-ms)
        start (- end window-ms)
        {:keys [notes]} @state-atom]
    (doseq [[key channels] notes]
      (doseq [[ch notes] channels]
        (doseq [{:keys [on off] :as note} notes]
          (draw-note key on off start end))))))

(defn track-note-event
  [notes command velocity timestamp]
  (let [notes (or notes [])]
    (cond
      (= :on command) (conj notes {:on timestamp :vel velocity})
      (= :off command) (let [idx (first (keep-indexed (fn [idx item] (when-not (:off item) idx)) notes))]
                         (cond-> notes
                           idx (assoc-in [idx :off] timestamp)))
      ;; FIXME: Handle "all notes off" CC
      :default notes)))

(defn purge-old-notes*
  [notes cutoff-ts]
  (filter (fn [{:keys [off] :as note}]
            (or (not off)
                (> cutoff-ts off)))
          notes))

(defn purge-old-notes
  [notes cutoff-ts]
  (reduce-kv
   (fn [acc key v]
     (update acc key reduce-kv (fn [acc ch notes]
                                 (update acc ch #(purge-old-notes* % cutoff-ts)))))
   {}
   notes))

(defn handle-midi-event
  [event-info _timestamp]
  ;; (println command event-info timestamp)
  (let [now (current-epoch-ms)
        {:keys [command channel key velocity]} event-info]
    (when (#{:on :off} command)
      (swap! state-atom update-in [:notes key channel] #(track-note-event % command velocity now)))
    (swap! state-atom update :notes purge-old-notes (- now window-ms))))

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
