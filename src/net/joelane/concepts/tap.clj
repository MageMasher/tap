(ns net.joelane.concepts.tap
  (:require [clojure.core.async :as a]))

(tap> 42)




(def ^:dynamic *spit-to-event-log* false)



(defn toggle-dynamic-var [dyn-var] (alter-var-root dyn-var not))
(defn toggle-spit-to-event-log [] (toggle-dynamic-var (var *spit-to-event-log*)))

(def spit-to-event-log (fn
                         spit-to-event-log
                         [x]
                         (when *spit-to-event-log*
                           (spit "event.log" (prn-str x) :append true))))


;; You must retain a reference to the function, otherwise you wont be able to
;; remove it. This means if you re-def a function before removing it you've
;; lost it and you can't remove it.

(add-tap spit-to-event-log)
(remove-tap spit-to-event-log)

;; Magic access
@(deref #'clojure.core/tapset)


;; Don't spit
(tap> (range 10))
(tap> (into [] (filter even?) (range 100)))

;; Toggle spit var
(toggle-spit-to-event-log)

;; Do spit
(tap> (range 10))
(tap> (into [] (filter even?) (range 100)))




;; From day-of-datomic-cloud in datomic.samples.repl
(defn- read-one
  [r]
  (try
    (read r)
    (catch java.lang.RuntimeException e
      (if (= "EOF while reading" (.getMessage e))
        ::EOF
        (throw e)))))

(defn read-all
  "Reads a sequence of top-level objects in file"
  ;; Modified from Clojure Cookbook, L Vanderhart & R. Neufeld
  [src]
  (with-open [r (java.io.PushbackReader. (clojure.java.io/reader src))]
    (binding [*read-eval* false]
      (doall (take-while #(not= ::EOF %) (repeatedly #(read-one r)))))))

(read-all "event.log")