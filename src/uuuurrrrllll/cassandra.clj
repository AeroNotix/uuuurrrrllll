(ns uuuurrrrllll.cassandra
  (:require [uuuurrrrllll.util :refer [gen-short-url]]
            [com.stuartsierra.component :as component])
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]
        [clojurewerkz.cassaforte.query]))


(def table "message")
(def pastes "pastes")

(defprotocol URLShortener
  (add-entry! [shortener body])
  (get-entry  [shortener short-code]))

(defrecord Cassandra [conn]
  component/Lifecycle
  (start [cass]
    (let [conn (client/connect ["localhost"])]
      (use-keyspace conn "uuuurrrrllll")
      (assoc cass :conn conn)))
  (stop [cass]
    (client/disconnect! conn))

  URLShortener
  (add-entry! [cass body]
    (let [short-url (gen-short-url 5)]
      (insert (:conn cass) table (assoc body :short_url short-url))
      short-url))
  (get-entry [cass short-code]
    (first
      (select (:conn cass)
        table
        (where [[= :short_url short-code]])
        (allow-filtering true)))))
