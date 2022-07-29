(ns hello-quil.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [hello-quil.midi-util :as midi]))

;; Ideally should be configurable instead of hard-coded
(def midi-device-description "IAC Driver IAC Bus 1")

;; FIXME make controllable by keyboard (toggle with V key)
(def velocity-sensitive? false)

(def state-atom (atom {:midi-dev nil
                       :notes {}
                       :note-counts {}}))

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
  (nth diatonic-colors (mod note 12))
  ;;(nth note-colors note)
  )

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

(defn percuss?
  [channel]
  (= 9 channel))

(defn kick? [k] (= 36 k))
(defn snare? [k]
  (#{38 40} k)
  ;;(= 40 k)
  )
(defn closed-hat? [k] (#{42 44} k))
(defn open-hat? [k] (= 46 k))
(defn crash? [k] (= 49 k))

(defn draw-note
  [{:keys [channel key velocity] :as note}]
  ;;(println "draw-note" note)
  (q/fill (note-key->color key))
  ;; FIXME: refactor, maybe using a multimethod
  (if-not (percuss? channel)
    (let [h (cond-> (q/height)
              velocity-sensitive? (* (/ velocity 127)))
          w (/ (q/width) 128)
          y (- (q/height) h)
          x (* key w)]
      (q/rect x y w h))
    (do
      (cond
        (kick? key)
        (let [size (* (q/height) 0.75)]
          (q/ellipse (/ (q/width) 2) (/ (q/height) 2) size size))

        (snare? key)
        (let [h (q/height)
              w (/ (q/width) 2)
              ;; FIXME: really reach into state-atom here???
              x (if (zero? (mod (get-in @state-atom [:note-counts key]) 2)) 0 w)
              y 0]
          (q/rect x y w h))

        (closed-hat? key)
        (do
          (q/fill 200)
          (let [h (/ (q/width) 128)
                y (- (* 0.75 (q/height)) (/ h 2))]
            (q/rect 0 y (q/width) h)))

        (open-hat? key)
        (do
          (q/fill 200)
          (let [h (/ (q/width) 128)
                y (- (* 0.25 (q/height)) (/ h 2))]
            (q/rect 0 y (q/width) h)))

        (crash? key)
        (q/background 255)

        :default
        (let [size (/ (q/height) 2)
              x (* (q/width) (if (zero? (mod key 2)) 0.25 0.75))
              y (* (q/height) (if (zero? (mod key 2)) 0.25 0.75))]
          (q/ellipse x y size size))))))

(defn clear-screen []
    (q/background 0))

(defn compare-notes
  [n1 n2]
  (let [ch1 (:channel n1)
        ch2 (:channel n2)
        k1 (:key n1)
        k2 (:key n2)]
    (if (= ch1 ch2)
      (if (percuss? ch1)
        ;; Prioritize kick (so it's above other percussion)
        (cond
          (kick? k1) 1
          (kick? k2) -1
          :default (compare k1 k2))
        (compare k1 k2))
      ;; Deprioritize percussion (so it's in behind other channels)
      (cond
        (percuss? ch1) -1
        (percuss? ch2) 1
        :default (compare ch1 ch2)))))

(defn draw []
  ;;(println "DRAW" @state-atom)
  (let [{:keys [notes]} @state-atom]
    (clear-screen)
    (doseq [note (sort compare-notes (vals notes))]
      (draw-note note))))

(defn handle-midi-event
  [event-info _timestamp]
  (let [{:keys [command channel key velocity]} event-info]
    ;;(println command event-info)
    (case command
      :on  (do (swap! state-atom update :notes assoc key event-info)
               (swap! state-atom update :note-counts update key (fnil inc 0)))
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
