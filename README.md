# Delegance

A Clojure library for evaluating code on a cluster of worker processes
that follow the [competing consumer][1] model.

Delegance is useful for running one-off calculations on a cluster
remote machines, without the overhead of something like Hadoop.

[1]: http://www.enterpriseintegrationpatterns.com/CompetingConsumers.html

## Installation

Add the following dependency to your `project.clj` file:

    [delegance "0.2.0"]

## Usage

To use Delegance you need a **queue** and a **key-value store**. For
testing and demonstration purposes, Delegance comes with an in-memory
version of both:

```clojure
(require '[delegance.memory :refer (memory-queue memory-store)])

(def config
  {:queue (memory-queue), :store (memory-store)})
```

In production, you'll want to extend the `Queue` and `KeyValueStore`
protocols in `delegance.protocols` to operate against a remote queue
and key-value store.

Once you have a configuration set up, start a worker process:

```clojure
(require '[delegance.worker :refer (run-worker shutdown-worker)])

(def worker
  (run-worker config))
```

You can set up multiple workers with the same configuration, and then
they will compete for messages sent by the client.

Now you have one worker running, you can delegate tasks to it:

```clojure
(require '[delegance.client :refer (delegate)])

(def result (delegate config `(+ 1 1)))
```

The result is a promise, and will be populated by the result of
evaluating the `(+ 1 1)` expression:

```clojure
user=> @result
2
```

If the expression is still being processed, the deref will block until
the result is calculated.


## License

Copyright © 2013 James Reeves

Distributed under the Eclipse Public License, the same as Clojure.
