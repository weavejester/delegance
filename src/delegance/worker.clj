(ns delegance.worker
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(def executor
  (ScheduledThreadPoolExecutor. 32))

(defn- every [rate runnable]
  (.scheduleAtFixedRate executor runnable 0 rate TimeUnit/SECONDS))

(defn- process-available-jobs [{:keys [queue state store]}]
  (when-let [job (reserve queue 300)]
    (try
      (eval (:data job))
      (finally (finish queue job)))))

(defn worker
  ([client]
     (worker 1 client))
  ([rate client]
     (every rate #(process-available-jobs client))))
