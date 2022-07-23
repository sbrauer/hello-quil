(ns util)

(defn hex->int
  [s]
  (Integer/parseInt s 16))

(defn hex->color
  [s]
  [(hex->int  (subs s 0 2))
   (hex->int  (subs s 2 4))
   (hex->int  (subs s 4 6))])
