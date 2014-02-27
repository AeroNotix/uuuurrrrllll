(ns uuuurrrrllll.transfer
  (:require [clojurewerkz.welle.buckets :as wb]
            [clojurewerkz.welle.core :as wc]
            [clojurewerkz.welle.kv :as kv]
            [uuuurrrrllll.core :refer [get-all-entries]])
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]
        [clojurewerkz.cassaforte.query]))


(def table "message")

(defn transfer []
  (insert-batch table (get-all-entries)))
