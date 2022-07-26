(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.midi-util :as midi]
            [hello-quil.colors :as colors]))

;; Ideally should be configurable instead of hard-coded
(def midi-device-description "IAC Driver IAC Bus 1")

(def state-atom (atom {:midi-dev nil
                       :notes {}}))

(defn note-key->color
  [note]
  ;;(nth diatonic-colors (mod note 12))
  (nth colors/atari-128-colors note))

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

(defn handle-midi-event
  [event-info _timestamp]
  (let [{:keys [command channel key velocity]} event-info]
    (println command event-info)
    (case command
      :on  (swap! state-atom update :notes assoc key event-info)
      :off (swap! state-atom update :notes dissoc key)
      :default)))

(defn setup
  []
  (setup-midi! midi-device-description handle-midi-event)
  (q/background 0) ; black
  ;; what's the lowest that looks ok?
  ;;(q/frame-rate 32)
  (q/frame-rate 16)
  (q/no-stroke))

(q/defsketch demo
  :title "midi quil fun"

  :size [640 480]

  ;;:size :fullscreen
  ;;:features [:keep-on-top]

  :setup setup
  :draw draw
  :on-close release-midi!)
