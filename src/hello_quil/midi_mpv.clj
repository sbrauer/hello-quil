(ns hello-quil.midi-mpv
  (:require [overtone.midi :as midi])
  ;;(:import [java.net Socket UnixDomainSocketAddress])
  (:import [jnr.unixsocket UnixSocketAddress UnixSocketChannel]
           [java.nio.channels Channels]
           [java.io PrintWriter])
  )

(def socket-path "/tmp/mpvsocket")
(def midi-device-name "IAC Driver IAC Bus 1")

;; Each device as a :description (which can be used to identify the device in a human-friendly way)
(comment
  (mapv :description (midi/midi-sources))
  )

(defn socket-writer
  [path]
  (let [address (UnixSocketAddress. socket-path)
        channel (UnixSocketChannel/open address)
        out (Channels/newOutputStream channel)]
    (PrintWriter. out)))

(def writer (socket-writer socket-path))

(defn mpv-command
  [writer s]
  ;; (println "mpv-command" s)
  (.write writer (str s "\n"))
  (.flush writer))

(def octave 12)

(def commands
  {0 {:note-on
      ;; DRY this up ofc
      {(+ (* 2 octave) 0) "script-message-to sammy safe-playlist-play-index 0"
       (+ (* 2 octave) 1) "script-message-to sammy safe-playlist-play-index 1"
       (+ (* 2 octave) 2) "script-message-to sammy safe-playlist-play-index 2"
       (+ (* 2 octave) 3) "script-message-to sammy safe-playlist-play-index 3"
       (+ (* 2 octave) 4) "script-message-to sammy safe-playlist-play-index 4"
       (+ (* 2 octave) 5) "script-message-to sammy safe-playlist-play-index 5"
       (+ (* 2 octave) 6) "script-message-to sammy safe-playlist-play-index 6"
       (+ (* 2 octave) 7) "script-message-to sammy safe-playlist-play-index 7"
       (+ (* 2 octave) 8) "script-message-to sammy safe-playlist-play-index 8"
       (+ (* 2 octave) 9) "script-message-to sammy safe-playlist-play-index 9"
       (+ (* 2 octave) 10) "script-message-to sammy safe-playlist-play-index 10"
       (+ (* 2 octave) 11) "script-message-to sammy safe-playlist-play-index 11"
       (+ (* 3 octave) 0) "seek 0 absolute"
       (+ (* 3 octave) 1) "seek 8 absolute-percent"
       (+ (* 3 octave) 2) "seek 16 absolute-percent"
       (+ (* 3 octave) 3) "seek 25 absolute-percent"
       (+ (* 3 octave) 4) "seek 33 absolute-percent"
       (+ (* 3 octave) 5) "seek 41 absolute-percent"
       (+ (* 3 octave) 6) "seek 50 absolute-percent"
       (+ (* 3 octave) 7) "seek 58 absolute-percent"
       (+ (* 3 octave) 8) "seek 66 absolute-percent"
       (+ (* 3 octave) 9) "seek 75 absolute-percent"
       (+ (* 3 octave) 10) "seek 83 absolute-percent"
       (+ (* 3 octave) 11) "seek 91 absolute-percent"


       ;; FIXME: Are these misc mappings good?  Maybe tweak. Possible ditch cycles and just have set commands (for mute or sub-visability)
       (+ (* 4 octave) 0) "cycle pause"
       (+ (* 4 octave) 1) "set pause yes"
       (+ (* 4 octave) 2) "ab-loop"
       (+ (* 4 octave) 3) "set pause no"
       (+ (* 4 octave) 4) "cycle mute"
       (+ (* 4 octave) 5) "cycle sub-visibility"

       ;; copied from https://github.com/mpv-player/mpv/blob/master/etc/input.conf
       (+ (* 4 octave) 6) "multiply speed 1/1.1" ;; F# slower
       (+ (* 4 octave) 8) "set speed 1.0" ;; G# reset to normal
       (+ (* 4 octave) 10) "multiply speed 1.1"  ;; A# faster
       ;; FIXME: Maybe also add an octave of specific speed settings, like we did seek.

       (+ (* 4 octave) 7) "set panscan 0.0" ;; G panscan off
       (+ (* 4 octave) 9) "set panscan 1.0" ;; A panscan on

       (+ (* 4 octave) 11) "playlist-shuffle"}}})

(def dev (midi/midi-in midi-device-name))

(midi/midi-handle-events
   dev
   (fn [{:keys [channel command data1] :as event}]
     ;; (println "we got midi!" event)
     (let [cmd (get-in commands [channel command data1])
           cmd (if (fn? cmd)
                 (cmd event)
                 cmd)]
       (when cmd
         (mpv-command writer cmd)))))


(comment
  (mpv-command writer "cycle pause")
  (mpv-command writer "script-message-to sammy safe-playlist-play-index 0")
  )



;; Note that currently we're focusing on receiving midi input.

(comment
  ;; list input devices (returns collection of maps)
  (midi/midi-sources)


  ;; Get the input device for a given description


  ;; Handle midi input events (note that an optional second fn may be specified to handle sysex events)


  ;; a few sample midi events (some fields omitted for brevity; all msgs have :timestamp and :device as well)
  {:channel 0 :command :note-on  :note 45 :velocity 100} ;; note-on/off also have :data1 (same as :note) and :data2 (:velocity)
  {:channel 0 :command :note-off :note 45 :velocity 64}
  {:channel 0 :command :control-change :data1 123 :data2 0} ;; :data1 is the CC# and :data2 is the value
  )
