(ns delegance.core
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer (random-uuid)]))

(deftype RemotePromise [getter poll-rate no-data]
  clojure.lang.IDeref
  (deref [rp]
    (loop []
      (let [value (getter)]
        (if (= value no-data)
          (do (Thread/sleep poll-rate) (recur))
          value)))))

(defn delegate-eval [client form]
  (let [{queue :queue, state :state} client
        job-id  (random-uuid)]
    (put state job-id {:form form :result ::no-data})
    (push queue job-id)
    (RemotePromise. #(:result (get! state job-id)) 1000 ::no-data)))

(defn resolve-form [form]
  (eval (read-string (str \` (pr-str form)))))

(defmacro delegate [client form]
  `(delegate-eval ~client '~(resolve-form form)))
