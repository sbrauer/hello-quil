(defproject hello-quil "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [quil "3.1.0"]
                 ;; for audio analysis (beat detection); maybe unneeded
                 [ddf.minim "2.2.0"]
                 ;; for midi support
                 [org.uncomplicate/clojure-sound "0.1.0"]
                 ])
