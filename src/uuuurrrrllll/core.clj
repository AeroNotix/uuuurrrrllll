(ns uuuurrrrllll.core
  (:require [com.stuartsierra.component :as component]
            [uuuurrrrllll.http :as http]
            [uuuurrrrllll.cassandra :as cass]))


(defn ->System []
  (component/system-map
    :database (cass/->Cassandra nil)
    :http     (component/using
                (http/->HTTPServer nil nil)
                [:database])))

(defn -main [& args]
  (.start (->System)))
