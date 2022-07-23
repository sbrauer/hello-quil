(ns hello-quil.midi-util
  (:require [uncomplicate.commons.core :as unc]
            [uncomplicate.clojure-sound.core :as cs]
            [uncomplicate.clojure-sound.midi :as midi]))

;; Note that currently we're focusing on receiving midi input.
;; If we extract some of this code to some generic lib,
;; consider whether transmitting midi output is in scope.

(defn input-device
  "Return input device for given description string"
  [dev-description]
  (->> (midi/device-info)
       (filter #(= dev-description  (unc/info % :description)))
       (map midi/device)
       (filter midi/transmitter?)
       first))

(defn input-devices
  "Returns a list of description strings for input devices"
  []
  (->> (midi/device-info)
       (map midi/device)
       (filter midi/transmitter?)
       (mapv #(unc/info % :description))))

(defn add-handler!
  "dev is a MidiInDevice object
  handler-fn takes an event info clj map and a timestamp long"
  ;; Note that the timestamp is in microseconds (millionths of a second).
  [dev handler-fn]
  (when-not (cs/open? dev)
    (cs/open! dev))
  (cs/connect! dev (midi/receiver
                    (fn [event timestamp]
                      (handler-fn (unc/info event) timestamp)))))

(defn clear-handlers!
  [dev]
  (unc/release (midi/transmitters dev)))

(defn close!
  [dev]
  (clear-handlers! dev)
  (when (cs/open? dev)
    ;; Seems that after closing a device, added handlers don't get called.
    ;; Furthermore, even a newly constructed Device for the same device
    ;; exhibits this behavior.  Not sure if this is by design or a bug.
    ;; Either way, only close a device when you're really done with it.
    (unc/close! dev)))

(comment
   ;; list our input devices
  (input-devices)

  ;; pick a device
  (def device-description "IAC Driver IAC Bus 1")
  (def dev (input-device device-description))

  ;; basic example handler
  (defn handler-fn
    [info ts]
    (println "HELLO" info ts))

  (add-handler! dev handler-fn)

  ;; Note that you can add multiple handlers.  This "releases" them.
  (clear-handlers! dev)

  ;; FIXME - figure out life cycle; currently after closing we can't get the handlers for the same hardware device to work again!
  ;; bonus: make it work with with-open fn
  (close! dev)
  )
