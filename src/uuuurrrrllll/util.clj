(ns uuuurrrrllll.util)


(def ^:private
  char-seq (doall
            (for [c (range 65 91)]
              (char c))))

(defn gen-short-url [n]
  (->> #(rand-nth char-seq)
       repeatedly
       (take n)
       clojure.string/join))
