(ns delegance.core
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer (random-uuid)]))

(deftype DelegateResult [client job-id]
  clojure.lang.IDeref
  (deref [_]
    (-> (:state client)
        (get! job-id)
        (:result))))

(defn delegate-eval [client form]
  (let [job-id (random-uuid)
        {:keys [queue state store]} client]
    (put state job-id {:form form})
    (push queue job-id)
    (DelegateResult. client job-id)))

(defmacro delegate [client form]
  `(delegate-eval ~client '~form))
