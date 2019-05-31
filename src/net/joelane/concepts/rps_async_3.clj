(ns net.joelane.concepts.rps_async_3
  (:require [clojure.core.async :as a]
            [clojure.spec.alpha :as s]))

(def moves ["rock" "paper" "scissors "])

(defn make-a-move []
  (keyword (first (shuffle moves))))

(defn rule [{p1 :player m1 :move :as play1}
            {p2 :player m2 :move :as play2}]
  (tap> [play1 play2])
  (case m1 :rock (case m2 :rock nil
                          :paper p2
                          :scissors p1)
           :paper (case m2 :rock p1
                           :paper nil
                           :scissors p2)
           :scissors (case m2 :rock p2
                              :paper p1
                              :scissors nil)))

(defn player []
  {:player (keyword (gensym "p"))
   :move   (make-a-move)})

(def judge-chan (a/chan 10))

(def judge-loop
  (dotimes [_ 10]
    (a/go-loop []
      (let [[hand-1 hand-2 result-chan] (a/<! judge-chan)
            winner (rule hand-1 hand-2)]
        (a/>! result-chan {:winner winner
                           :h1     hand-1
                           :h2     hand-2})
        (recur)))))

(defn judge [moves]
  (let [result-chan (a/chan)]
    (a/>!! judge-chan (conj moves result-chan))
    result-chan))

(defn play []
  (let [game (a/chan)]
    (a/go (a/>! game [(player) (player)]))
    (println (a/<!! (judge (a/<!! game))))))


(comment
  (play)

  (rule (player) (player))


  (def process-log (agent []))
  (defn process-logger [x] (send process-log conj x))
  (defn clear-process-log [] (send process-log (fn [a] [])))
  (defn deref-process-log [] @process-log)



  (deref-process-log)
  (def the-log (deref-process-log))

  (add-tap process-logger)

  ;1
  (do
    (dotimes [n 100]
      (play)))
  (count (deref-process-log))

  ;2
  (do
    (dotimes [n 100]
      (future (play))))
  (count (deref-process-log))

  (take 3 (deref-process-log))

  ;3
  (do
    (future (dotimes [n 100]
              (play))))
  (count (deref-process-log))



  ;;;;;;;;;;;;;;;;;;;;


  (add-tap clojure.pprint/pprint)
  (remove-tap clojure.pprint/pprint)



  ;;;;;;;;;;;;;;;;;;;;
  (require '[clojure.spec.alpha :as s])


  (s/def ::player keyword?)
  (s/def ::move #{:scissors :rock :paper})
  (s/def ::play (s/keys :req-un [::player ::move]))
  (s/def ::plays (s/coll-of ::play))
  (s/def ::plays-log (s/coll-of ::plays))


  (s/check-asserts true)

  (def the-log (deref-process-log))

  (s/valid? ::plays-log the-log)


  (clojure.pprint/pprint (remove #(s/valid? ::plays %) the-log))

  (defn tap>spec-explain
    [x]
    (when-let [d (s/explain-data ::plays x)]
      (clojure.pprint/pprint d)))

  (add-tap tap>spec-explain)
  (remove-tap tap>spec-explain)


  ;;;;;;;;;;;;;;;;;;;;

  )