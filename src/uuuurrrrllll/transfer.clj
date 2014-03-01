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
  (spit "f" "[")
  (doall (for [entry (get-all-entries)]
           (do (spit "f" entry :append true)
               (spit "f" "," :append true))))
  (spit "f" "]" :append true))

(defn transfer-from-file []
  (insert-batch table (read-string (slurp "f"))))
