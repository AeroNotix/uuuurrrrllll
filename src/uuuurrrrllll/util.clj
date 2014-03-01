(ns uuuurrrrllll.util)


(def ^:private
  char-seq (doall
            (for [c (range 65 91)]
              (char c))))

(defn merge-urls [grouped-urls]
  (for [[k v] grouped-urls]
    [k (map :url v)]))

(defn coalesce-entries [getter]
  (->> (getter)
       (group-by :channel)
       (merge-urls)))

(defn gen-short-url [n]
  (->> #(rand-nth char-seq)
       repeatedly
       (take n)
       clojure.string/join))
