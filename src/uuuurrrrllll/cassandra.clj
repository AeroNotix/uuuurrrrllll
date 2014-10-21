(ns uuuurrrrllll.cassandra
  (:require [uuuurrrrllll.util :refer [gen-short-url]])
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]
        [clojurewerkz.cassaforte.query]))


(def table "message")
(def pastes "pastes")

(defn get-all-entries []
  (select table))

(defn add-entry! [body]
  (let [short-url (gen-short-url 5)]
    (insert table (assoc body :short_url short-url) (using :ttl 600))
    short-url))

(defn add-text! [text]
  (let [short-code (gen-short-url 7)]
    (insert pastes {:short_code short-code :message text} (using :ttl 3600))
    short-code))

(defn get-entry [short]
  (first
   (select table (where :short_url short) (allow-filtering true))))

(defn get-text-entry [short]
  (first
   (select pastes (where :short_code short) (allow-filtering true))))
