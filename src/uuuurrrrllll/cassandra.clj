(ns uuuurrrrllll.cassandra
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]
        [clojurewerkz.cassaforte.query]))


(def table "message")

(defn get-all-entries []
  (select table))
