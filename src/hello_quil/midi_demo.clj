(ns medu-demo
  (:require [uncomplicate.commons.core :as unc]
            [uncomplicate.clojure-sound.core :as cs]
            [uncomplicate.clojure-sound.midi :as midi]))

;; Specific to Sam's setup; should be a config
(def midi-device-description "MIDISPORT 4x4 Anniv Port A")
;;(def midi-device-description "MidiKeys")
 (def midi-device-description "IAC Driver IAC Bus 1")

(def midi-device
  (->> (midi/device-info)
       (filter #(= midi-device-description  (unc/info % :description)))
       (map midi/device)
       (filter midi/transmitter?)
       first))

(cs/open! midi-device)
(cs/connect! midi-device
             (midi/receiver (partial println "Hello")))
(comment
  ;; Example output:
  Hello {:channel 0, :command :on, :key 64, :velocity 100} 361477144440
  Hello {:channel 0, :command :off, :key 64, :velocity 64} 361477269453
  Hello {:channel 0, :command :on, :key 60, :velocity 100} 361477644441
  Hello {:channel 0, :command :off, :key 60, :velocity 64} 361477769453
  Hello {:channel 0, :command :cc, :controller 123, :value 0, :control :all-notes-off} 361477783852
)
