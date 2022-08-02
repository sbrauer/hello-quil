(ns util
  (:require [clojure.string :as str]))

(defn hex->int
  [s]
  (Integer/parseInt s 16))

(defn- strip-hash
  [s]
  (if (str/starts-with? s "#")
    (subs s 1)
    s))

(defn hex->color
  [s]
  (let [s (strip-hash s)]
    [(hex->int  (subs s 0 2))
     (hex->int  (subs s 2 4))
     (hex->int  (subs s 4 6))]))
