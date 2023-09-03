(defproject hello-quil "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [quil "3.1.0"]
                 ;; for midi support
                 [overtone/midi-clj "0.5.0"]
                 ;; for animated gif output
                 [gil "1.0.0-SNAPSHOT"]
                 ;; FIXME: for unix domain sockets
                 [com.github.jnr/jnr-unixsocket "0.38.20"]])
