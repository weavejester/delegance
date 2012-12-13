(ns delegance.core
  (:require [delegance.protocols :refer :all]))

(defn delegate-eval [client form]
  (push (:queue client) form))

(defmacro delegate [client form]
  `(delegate-eval ~client '~form))
