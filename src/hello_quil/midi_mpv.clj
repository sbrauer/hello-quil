(ns hello-quil.midi-mpv
  (:require [overtone.midi :as midi])
  ;;(:import [java.net Socket UnixDomainSocketAddress])
  (:import [jnr.unixsocket UnixSocketAddress UnixSocketChannel]
           [java.nio.channels Channels]
           [java.io PrintWriter])
  )

(def socket-path "/tmp/mpvsocket")
(def midi-device-name "IAC Driver IAC Bus 1")

(comment
  ;; Each device as a :description (which can be used to identify the device in a human-friendly way)
  (mapv :description (midi/midi-sources))
  ;; FIXME: Maybe add a command line option like `--list-devices` that prints these two stdout.
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
  (println "mpv-command" s)
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

       ;; FIXME: Are these misc mappings good?  Maybe tweak. Used to have a few cycle commands, but replaced with sets.

       (+ (* 4 octave) 0) "set pause yes"          ;; C pause
       (+ (* 4 octave) 2) "set pause no"           ;; D play
       (+ (* 4 octave) 1) "set panscan 0.0"        ;; C# panscan off
       (+ (* 4 octave) 3) "set panscan 1.0"        ;; D# panscan on
       (+ (* 4 octave) 4) "set mute yes"           ;; E mute
       (+ (* 4 octave) 5) "set mute no"            ;; F unmute
       (+ (* 4 octave) 7) "set sub-visibility no"  ;; G subs off
       (+ (* 4 octave) 9) "set sub-visibility yes" ;; A subs on

       ;; (+ (* 4 octave) 6) "multiply speed 1/1.1" ;; F# slower
       ;; (+ (* 4 octave) 8) "set speed 1.0"        ;; G# reset to normal
       ;; (+ (* 4 octave) 10) "multiply speed 1.1"  ;; A# faster

       (+ (* 4 octave) 6) "seek -5" ;; F# jump back 5 secs like left arrow key
       (+ (* 4 octave) 8) "cycle pause" ;; G# toggle pause
       (+ (* 4 octave) 10) "seek 5" ;; A# jump forward 5 secs like right arrow key

       (+ (* 4 octave) 11) "ab-loop" ;; B ab-loop start/end/off

       ;; Absolute speed settings. Maybe tweak some of these...
       (+ (* 5 octave) 0) "set speed 0.1"
       (+ (* 5 octave) 1) "set speed 0.2"
       (+ (* 5 octave) 2) "set speed 0.35"
       (+ (* 5 octave) 3) "set speed 0.5"
       (+ (* 5 octave) 4) "set speed 0.7"
       (+ (* 5 octave) 5) "set speed 0.85"
       (+ (* 5 octave) 6) "set speed 1.0" ;; normal
       (+ (* 5 octave) 7) "set speed 1.25"
       (+ (* 5 octave) 8) "set speed 1.5"
       (+ (* 5 octave) 9) "set speed 1.75"
       (+ (* 5 octave) 10) "set speed 2.0"
       (+ (* 5 octave) 11) "set speed 3.0"
       (+ (* 5 octave) 12) "set speed 4.0"}}})

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
