(ns net.joelane.concepts.rps_async_2
  (:require [clojure.core.async :as a]))

(def moves ["rock" "paper" "scissors"])

(defn make-a-move []
  (keyword (first (shuffle moves))))

(defn rule [{p1 :player m1 :move}
            {p2 :player m2 :move}]
  (case m1 :rock (case m2 :rock nil
                          :paper p2
                          :scissors p1)
           :paper (case m2 :rock p1
                           :paper nil
                           :scissors p2)
           :scissors (case m2 :rock p2
                              :paper p1
                              :scissors nil)))

(defn player [game]
  (a/go (let [value {:player (keyword (gensym "p"))
                     :move   (make-a-move)}]
          (tap> value)
          (a/>! game value))))

(defn judge [game]
  (a/<!! (a/go (let [hand-1 (a/<! game)
                 hand-2 (a/<! game)
                 winner (rule hand-1 hand-2)]
                 (tap> winner)
             {:winner winner
              :h1 hand-1
              :h2 hand-2}))))

(defn play []
  (let [game (a/chan)]

    (a/go (player game))
    (a/go (player game))
    (println (judge game))))


(comment

  (add-tap clojure.pprint/pprint)
  (remove-tap clojure.pprint/pprint)
  (play)

  )