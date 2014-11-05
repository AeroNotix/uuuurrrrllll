(ns uuuurrrrllll.cassandra
  (:require [uuuurrrrllll.util :refer [gen-short-url]]
            [com.stuartsierra.component :as component])
  (:use [clojurewerkz.cassaforte.client :as client]
        [clojurewerkz.cassaforte.cql]
        [clojurewerkz.cassaforte.query]))


(def default-hosts ["localhost"])
(def keyspace "uuuurrrrllll")
(def pastes "pastes")
(def table "message")
(def short-code-length 5)

(defprotocol URLShortener
  (add-entry! [shortener body])
  (get-entry  [shortener short-code]))

(defrecord Cassandra [conn]
  component/Lifecycle
  (start [cass]
    (let [conn (client/connect default-hosts)]
      (use-keyspace conn keyspace)
      (assoc cass :conn conn)))
  (stop [cass]
    (client/disconnect! conn))

  URLShortener
  (add-entry! [cass body]
    (let [short-url (gen-short-url short-code-length)]
      (insert (:conn cass) table (assoc body :short_url short-url))
      short-url))
  (get-entry [cass short-code]
    (first
      (select (:conn cass)
        table
        (where [[= :short_url short-code]])
        (allow-filtering true)))))
