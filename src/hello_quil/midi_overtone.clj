(ns hello-quil.midi-overtone
  (:require [overtone.midi :as midi]))

;; Note that currently we're focusing on receiving midi input.

(comment
  ;; list input devices (returns collection of maps)
  (midi/midi-sources)

  ;; Each device as a :description (which can be used to identify the device in a human-friendly way)
  (mapv :description (midi/midi-sources))

  ;; Get the input device for a given description
  (def dev (midi/midi-in  "IAC Driver IAC Bus 1"))

  ;; Handle midi input events (note that an optional second fn may be specified to handle sysex events)
  (midi/midi-handle-events
   dev
   (fn [event]
     (println "demo-handler" event)))

  ;; a few sample midi events (some fields omitted for brevity; all msgs have :timestamp and :device as well)
  {:channel 0 :command :note-on  :note 45 :velocity 100} ;; note-on/off also have :data1 (same as :note) and :data2 (:velocity)
  {:channel 0 :command :note-off :note 45 :velocity 64}
  {:channel 0 :command :control-change :data1 123 :data2 0} ;; :data1 is the CC# and :data2 is the value
  )
